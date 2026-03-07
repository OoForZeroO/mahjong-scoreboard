package com.mahjong.service.impl;

import com.mahjong.model.*;
import com.mahjong.repository.MatchRepository;
import com.mahjong.repository.MatchParticipantRepository;
import com.mahjong.repository.RoundScoreRepository;
import com.mahjong.repository.MatchResultRepository;
import com.mahjong.repository.UserRepository;
import com.mahjong.repository.WechatUserRepository;
import com.mahjong.repository.MatchSettlementRepository;
import com.mahjong.service.MatchService;
import com.mahjong.service.AvatarUploadService;
import com.mahjong.dto.ParticipantScoreInfo;
import com.mahjong.dto.MatchResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;

@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);

    @Autowired
    private MatchRepository dao;

    @Autowired
    private MatchParticipantRepository pdao;

    @Autowired
    private RoundScoreRepository rdao;

    @Autowired
    private MatchResultRepository mdao;


    @Autowired
    private UserRepository udao;

    @Autowired
    private WechatUserRepository wdao;
    
    @Autowired
    private AvatarUploadService avatarUploadService;
    
    // 暂时注释掉MatchSettlementRepository，因为数据库中没有match_settlements表
    // @Autowired
    // private MatchSettlementRepository sdao;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 头像地址规范化：
     * - 去掉前后空格
     * - 丢弃 wxfile:// 和 http(s)://tmp/ 这类临时地址（返回 null）
     */
    private String sanitizeAvatar(String avatar) {
        if (avatar == null) {
            return null;
        }
        String url = avatar.trim();
        if (url.isEmpty()) {
            return null;
        }
        // 小程序本地/临时文件路径，不应作为最终头像返回或持久化
        if (url.startsWith("wxfile://") || url.contains("://tmp/")) {
            logger.warn("检测到临时头像地址，将丢弃: {}", url);
            return null;
        }
        return url;
    }

    /**
     * 将前端传的头像转为可持久化的 https 地址：临时地址先尝试下载并保存，非临时原样返回。
     */
    private String normalizeAvatarForSave(String avatar) {
        if (avatar == null || avatar.trim().isEmpty()) {
            return null;
        }
        String converted = avatarUploadService.convertTempAvatarUrlToHttps(avatar);
        if (converted != null) {
            return converted;
        }
        return sanitizeAvatar(avatar);
    }

    // 对局相关方法
    @Override
    public Match createMatch(Match m) {
        try {
            // 如果没有提供roomName，使用默认值
            if (m.getRoomName() == null || m.getRoomName().trim().isEmpty()) {
                m.setRoomName("默认房间");
            }
            
            // 设置开始时间
            if (m.getStartTime() == null) {
                m.setStartTime(System.currentTimeMillis());
            }
            
            return dao.save(m);
        } catch (Exception e) {
            logger.error("创建对局失败", e);
            throw e;
        }
    }

    @Override
    public Optional<Match> getMatchById(Long id) {
        return dao.findById(id);
    }

    @Override
    public MatchDetailResponse getMatchDetail(Long matchId) {
        Optional<Match> matchOpt = dao.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new IllegalArgumentException("对局不存在，matchId: " + matchId);
        }
        
        Match match = matchOpt.get();
        MatchDetailResponse response = new MatchDetailResponse();
        
        // 设置对局基本信息
        logger.info("获取对局详情 - matchId: {}, totalRounds: {}, status: {}", 
                   matchId, match.getTotalRounds(), match.getStatus());
        response.setMatchId(match.getMatchId());
        response.setRoomName(match.getRoomName());
        response.setTotalRounds(match.getTotalRounds());
        Integer currentRound = getCurrentRoundNumber(matchId);
        logger.info("获取对局详情 - matchId: {}, currentRound: {}", matchId, currentRound);
        response.setCurrentRound(currentRound);
        response.setMatchStatus(match.getStatus());
        response.setCreateTime(match.getCreateTime());
        response.setUpdateTime(match.getUpdateTime());
        
        // 获取参与者数据
        List<MatchParticipant> participants = pdao.findByMatch(match);
        List<ParticipantDetail> participantDetails = new ArrayList<>();
        
        for (MatchParticipant participant : participants) {
            ParticipantDetail detail = new ParticipantDetail();
            detail.setParticipantId(participant.getId());
            detail.setTotalScore(participant.getTotalScore());
            detail.setWechatUserId(participant.getWechatUserId());

            // 约定：participant.wechat_user_id = wechat_users 表主键 id；能查到则 isVisitor=false 并回填用户信息
            WechatUser wechatUser = resolveWechatUser(participant.getWechatUserId());
            if (wechatUser != null) {
                detail.setIsVisitor(wechatUser.getIsVisitor() != null ? wechatUser.getIsVisitor() : false);
                detail.setUserId(wechatUser.getId());
                detail.setNickname(wechatUser.getNickname());
                // 头像：优先 wechat_users，再 participant 关联的 user，再 match_participants.avatar，避免返回 null 时前端缺图
                String avatar = pickAvatar(wechatUser, participant);
                detail.setAvatar(avatar != null ? avatar : "");
            } else {
                detail.setIsVisitor(true);
                detail.setUserId(null);
                detail.setNickname(participant.getUserName());
                String avatar = participant.getUser() != null ? participant.getUser().getAvatar() : null;
                if (avatar == null) avatar = participant.getAvatar();
                detail.setAvatar(avatar != null ? avatar : "");
            }

            Boolean isQuit = participant.getIsQuit() != null ? participant.getIsQuit() : false;
            detail.setIsQuit(isQuit);
            logger.debug("参与者详情 - participantId: {}, nickname: {}, totalScore: {}, isQuit: {}, isVisitor: {}",
                        participant.getId(), detail.getNickname(), participant.getTotalScore(), isQuit, detail.getIsVisitor());

            participantDetails.add(detail);
        }
        response.setParticipants(participantDetails);
        
        // 获取轮次数据
        List<RoundScore> roundScores = rdao.findByMatchOrderByRoundNumberAsc(match);
        Map<Integer, MatchDetailResponse.RoundDetail> roundMap = new LinkedHashMap<>();
        
        for (RoundScore roundScore : roundScores) {
            Integer roundNumber = roundScore.getRoundNumber();
            
            if (!roundMap.containsKey(roundNumber)) {
                MatchDetailResponse.RoundDetail roundDetail = new MatchDetailResponse.RoundDetail();
                roundDetail.setRoundNumber(roundNumber);
                roundDetail.setRoundTime(roundScore.getRoundTime());
                roundDetail.setScores(new ArrayList<>());
                roundMap.put(roundNumber, roundDetail);
            }
            
            MatchDetailResponse.RoundScoreDetail scoreDetail = new MatchDetailResponse.RoundScoreDetail();
            scoreDetail.setParticipantId(roundScore.getParticipant().getId());
            scoreDetail.setParticipantName(roundScore.getParticipant().getUserName());
            scoreDetail.setScore(roundScore.getScore());
            scoreDetail.setCumulativeScore(roundScore.getCumulativeScore());
            
            roundMap.get(roundNumber).getScores().add(scoreDetail);
        }
        
        response.setRounds(new ArrayList<>(roundMap.values()));
        
        logger.info("获取对局详情完成 - matchId: {}, participants: {}, rounds: {}", 
                   matchId, participantDetails.size(), roundMap.size());
        logger.debug("对局详情 - matchId: {}, totalRounds: {}, currentRound: {}, status: {}, roomName: {}", 
                    matchId, response.getTotalRounds(), response.getCurrentRound(), 
                    response.getMatchStatus(), response.getRoomName());
        
        return response;
    }

    @Override
    public List<Match> getAllMatches() {
        return dao.findAll();
    }

    /**
     * 根据参与者的 wechat_user_id 解析 WechatUser。
     * 约定：wechat_user_id 存的是 wechat_users 表主键 id；能查到则视为已注册用户。
     * 兼容：若解析为数字则按 id 查，否则按 openid(userId) 查历史数据。
     */
    private WechatUser resolveWechatUser(String wechatUserId) {
        if (wechatUserId == null || wechatUserId.trim().isEmpty()) {
            return null;
        }
        try {
            Long id = Long.parseLong(wechatUserId.trim());
            return wdao.findById(id).orElseGet(() -> wdao.findByUserId(wechatUserId));
        } catch (NumberFormatException e) {
            return wdao.findByUserId(wechatUserId);
        }
    }

    /**
     * 从 WechatUser 与 MatchParticipant 中选取头像（优先 wechat_users，再 participant.user，再 participant.avatar）。
     * 返回非空字符串或 null（调用方可用 "" 作为兜底以便接口始终返回头像字段）。
     */
    private String pickAvatar(WechatUser wechatUser, MatchParticipant participant) {
        if (wechatUser != null && wechatUser.getAvatar() != null && !wechatUser.getAvatar().trim().isEmpty()) {
            return wechatUser.getAvatar();
        }
        if (participant != null && participant.getUser() != null && participant.getUser().getAvatar() != null && !participant.getUser().getAvatar().trim().isEmpty()) {
            return participant.getUser().getAvatar();
        }
        if (participant != null && participant.getAvatar() != null && !participant.getAvatar().trim().isEmpty()) {
            return participant.getAvatar();
        }
        return null;
    }

    @Override
    public List<Match> getMatchesByRoomId(Long id) {
        // 这里需要根据roomId查找Room实体，然后查询Match
        // 暂时返回空列表，需要RoomRepository支持
        return Collections.emptyList();
    }

    @Override
    public Match updateMatch(Long id, Match m) {
        Optional<Match> e = dao.findById(id);
        if (e.isPresent()) {
            Match u = e.get();
            if (m.getRoomName() != null) {
                u.setRoomName(m.getRoomName());
            }
            if (m.getStatus() != null) {
                u.setStatus(m.getStatus());
            }
            if (m.getTotalRounds() != null) {
                u.setTotalRounds(m.getTotalRounds());
            }
            if (m.getSettlementMultiplier() != null) {
                u.setSettlementMultiplier(m.getSettlementMultiplier());
            }
            if (Integer.valueOf(1).equals(m.getStatus()) && u.getEndTime() == null) {
                u.setEndTime(System.currentTimeMillis());
            }
            
            return dao.save(u);
        }
        return null;
    }

    @Override
    @Transactional
    public Match endMatch(Long id) {
        logger.info("=== 开始收盘对局 ===");
        logger.info("对局ID: {}", id);
        
        Optional<Match> e = dao.findById(id);
        if (e.isPresent()) {
            Match u = e.get();
            logger.info("找到对局记录: matchId={}, status={}, totalRounds={}", 
                       u.getMatchId(), u.getStatus(), u.getTotalRounds());
            
            u.setStatus(1); // 1:已完成
            u.setEndTime(System.currentTimeMillis());
            logger.info("更新对局状态为已完成，设置结束时间: {}", u.getEndTime());
            
            // 计算并保存对局结果
            logger.info("开始计算对局结果...");
            MatchParticipant w = getMatchWinner(id);
            logger.info("获胜者: {}", w != null ? w.getId() : "无");
            
            // 删除现有的对局结果（如果存在），然后创建新的
            logger.info("删除现有的对局结果记录...");
            mdao.deleteByMatchId(id);
            logger.info("已删除现有对局结果记录，对局ID: {}", id);
            
            // 创建新的对局结果
            logger.info("创建新的对局结果记录...");
            MatchResult r = new MatchResult();
            r.setMatchId(id);
            logger.info("设置MatchResult的matchId: {}", id);
            
            // 验证matchId不为null
            if (r.getMatchId() == null) {
                logger.error("CRITICAL: MatchResult has null matchId for match: {}, this will cause database error!", id);
                throw new IllegalStateException("无法为MatchResult设置matchId，对局ID: " + id);
            }
            
            r.setWinner(w);
            logger.info("设置获胜者: {}", w != null ? w.getId() : "无");
            
            // 计算最高分和最低分
            logger.info("开始计算最高分和最低分...");
            List<MatchParticipant> ps = pdao.findByMatch(u);
            logger.info("找到参与者数量: {}", ps.size());
            
            Integer h = null;
            Integer l = null;
            
            for (MatchParticipant p : ps) {
                Integer s = p.getTotalScore();
                logger.info("参与者ID: {}, 总得分: {}", p.getId(), s);
                if (h == null || s > h) h = s;
                if (l == null || s < l) l = s;
            }
            
            logger.info("计算完成 - 最高分: {}, 最低分: {}", h, l);
            r.setHighestScore(h);
            r.setLowestScore(l);
            
            // 设置完成时间和总时长
            logger.info("开始设置时间和轮次信息...");
            long completionTime = System.currentTimeMillis();
            r.setCompletionTime(completionTime);
            logger.info("设置完成时间: {}", completionTime);
            
            // 计算总时长（毫秒）- 当前时间与match表的创建时间相减
            if (u.getCreateTime() != null) {
                long totalDuration = completionTime - u.getCreateTime();
                r.setTotalDuration(totalDuration);
                logger.info("计算总时长: {} ms (基于创建时间)", totalDuration);
            } else if (u.getStartTime() != null) {
                // 如果createTime不存在，使用startTime作为备选
                long totalDuration = completionTime - u.getStartTime();
                r.setTotalDuration(totalDuration);
                logger.info("计算总时长: {} ms (基于开始时间)", totalDuration);
            } else {
                logger.warn("无法计算总时长，createTime和startTime都为空");
            }
            
            // 不需要设置total_rounds字段
            logger.info("跳过total_rounds字段设置");
            
            // 生成参与者得分信息的JSON数据
            logger.info("开始生成参与者得分信息JSON...");
            try {
                List<ParticipantScoreInfo> participantScores = new ArrayList<>();
                for (MatchParticipant p : ps) {
                    logger.info("处理参与者: ID={}, 总得分={}", p.getId(), p.getTotalScore());
                    
                    // 计算倍率后的最终得分
                    Integer finalScore = p.getTotalScore();
                    if (u.getSettlementMultiplier() != null && u.getSettlementMultiplier() > 0) {
                        finalScore = (int) Math.round(p.getTotalScore() * u.getSettlementMultiplier());
                        logger.info("应用结算倍率: {} -> {}", p.getTotalScore(), finalScore);
                    }
                    
                    // 获取用户信息
                    String nickname = p.getUserName();
                    String avatar = p.getAvatar(); // 直接从MatchParticipant获取头像
                    String wechatUserId = p.getWechatUserId();
                    
                    if (p.getUser() != null) {
                        // 如果User对象存在，优先使用User中的信息
                        nickname = p.getUser().getNickname();
                        if (p.getUser().getAvatar() != null && !p.getUser().getAvatar().trim().isEmpty()) {
                            avatar = p.getUser().getAvatar();
                        }
                        wechatUserId = p.getUser().getUserId();
                        logger.info("从User对象获取信息: 昵称={}, 头像={}", nickname, avatar);
                    } else {
                        logger.info("使用MatchParticipant中的信息: 昵称={}, 头像={}", nickname, avatar);
                    }

                    // 对头像地址做规范化处理，避免返回 wxfile://、http://tmp/ 等临时地址
                    avatar = sanitizeAvatar(avatar);
                    
                    ParticipantScoreInfo scoreInfo = new ParticipantScoreInfo(
                        p.getId(),
                        nickname,
                        avatar,
                        p.getTotalScore(),
                        finalScore,
                        p.equals(w), // 是否为获胜者
                        wechatUserId
                    );
                    participantScores.add(scoreInfo);
                    logger.info("添加参与者得分信息: ID={}, 昵称={}, 最终得分={}, 是否获胜={}", 
                               p.getId(), nickname, finalScore, p.equals(w));
                }
                
                // 序列化为JSON
                String totalScoresJson = objectMapper.writeValueAsString(participantScores);
                r.setTotalScores(totalScoresJson);
                logger.info("成功生成参与者得分信息JSON，长度: {}", totalScoresJson.length());
                logger.debug("JSON内容: {}", totalScoresJson);
                
            } catch (Exception ex) {
                logger.error("生成参与者得分信息JSON失败", ex);
                r.setTotalScores("[]"); // 设置空数组作为默认值
            }
            
            logger.info("开始保存MatchResult到数据库...");
            mdao.save(r);
            logger.info("MatchResult保存成功，matchId: {}", r.getMatchId());
            
            logger.info("开始保存Match到数据库...");
            Match savedMatch = dao.save(u);
            logger.info("Match保存成功，matchId: {}", savedMatch.getMatchId());
            
            logger.info("=== 收盘对局完成 ===");
            return savedMatch;
        }
        return null;
    }

    @Override
    @Transactional
    public Match endMatch(Long id, EndMatchRequest request) {
        logger.info("=== 开始收盘对局（带请求参数） ===");
        logger.info("对局ID: {}, 请求参数: {}", id, request);
        
        Optional<Match> e = dao.findById(id);
        if (e.isPresent()) {
            Match u = e.get();
            logger.info("找到对局记录: matchId={}, status={}, totalRounds={}", 
                       u.getMatchId(), u.getStatus(), u.getTotalRounds());
            
            u.setStatus(1); // 1:已完成
            u.setEndTime(System.currentTimeMillis());
            logger.info("更新对局状态为已完成，设置结束时间: {}", u.getEndTime());
            
            // 处理roomName（仅作为显示名称，不涉及房间管理）
            if (request.getRoomName() != null && !request.getRoomName().trim().isEmpty()) {
                u.setRoomName(request.getRoomName());
                logger.info("设置房间名称: {}", request.getRoomName());
            }
            
            // 计算并保存对局结果
            logger.info("开始计算对局结果...");
            MatchParticipant w = getMatchWinner(id);
            logger.info("获胜者: {}", w != null ? w.getId() : "无");
            
            // 删除现有的对局结果（如果存在），然后创建新的
            logger.info("删除现有的对局结果记录...");
            mdao.deleteByMatchId(id);
            logger.info("已删除现有对局结果记录，对局ID: {}", id);
            
            // 创建新的对局结果
            logger.info("创建新的对局结果记录...");
            MatchResult r = new MatchResult();
            r.setMatchId(id);
            logger.info("设置MatchResult的matchId: {}", id);
            
            r.setWinner(w);
            logger.info("设置获胜者: {}", w != null ? w.getId() : "无");
            
            // 计算最高分和最低分
            List<MatchParticipant> ps = pdao.findByMatch(u);
            Integer h = null;
            Integer l = null;
            
            for (MatchParticipant p : ps) {
                Integer s = p.getTotalScore();
                if (h == null || s > h) h = s;
                if (l == null || s < l) l = s;
            }
            
            r.setHighestScore(h);
            r.setLowestScore(l);
            
            // 设置收盘倍率（如果有的话）
            if (request.getMultiplier() != null) {
                u.setSettlementMultiplier(request.getMultiplier());
                logger.info("Match ended with multiplier: {}", request.getMultiplier());
            }
            
            // 设置完成时间和总时长
            logger.info("开始设置时间和轮次信息...");
            long completionTime = System.currentTimeMillis();
            r.setCompletionTime(completionTime);
            logger.info("设置完成时间: {}", completionTime);
            
            // 计算总时长（毫秒）- 当前时间与match表的创建时间相减
            if (u.getCreateTime() != null) {
                long totalDuration = completionTime - u.getCreateTime();
                r.setTotalDuration(totalDuration);
                logger.info("计算总时长: {} ms (基于创建时间)", totalDuration);
            } else if (u.getStartTime() != null) {
                // 如果createTime不存在，使用startTime作为备选
                long totalDuration = completionTime - u.getStartTime();
                r.setTotalDuration(totalDuration);
                logger.info("计算总时长: {} ms (基于开始时间)", totalDuration);
            } else {
                logger.warn("无法计算总时长，createTime和startTime都为空");
            }
            
            // 不需要设置total_rounds字段
            logger.info("跳过total_rounds字段设置");
            
            // 生成参与者得分信息的JSON数据
            logger.info("开始生成参与者得分信息JSON...");
            try {
                List<ParticipantScoreInfo> participantScores = new ArrayList<>();
                for (MatchParticipant p : ps) {
                    logger.info("处理参与者: ID={}, 总得分={}", p.getId(), p.getTotalScore());
                    
                    // 计算倍率后的最终得分
                    Integer finalScore = p.getTotalScore();
                    if (u.getSettlementMultiplier() != null && u.getSettlementMultiplier() > 0) {
                        finalScore = (int) Math.round(p.getTotalScore() * u.getSettlementMultiplier());
                        logger.info("应用结算倍率: {} -> {}", p.getTotalScore(), finalScore);
                    }
                    
                    // 获取用户信息
                    String nickname = p.getUserName();
                    String avatar = p.getAvatar(); // 直接从MatchParticipant获取头像
                    String wechatUserId = p.getWechatUserId();
                    
                    if (p.getUser() != null) {
                        // 如果User对象存在，优先使用User中的信息
                        nickname = p.getUser().getNickname();
                        if (p.getUser().getAvatar() != null && !p.getUser().getAvatar().trim().isEmpty()) {
                            avatar = p.getUser().getAvatar();
                        }
                        wechatUserId = p.getUser().getUserId();
                        logger.info("从User对象获取信息: 昵称={}, 头像={}", nickname, avatar);
                    } else {
                        logger.info("使用MatchParticipant中的信息: 昵称={}, 头像={}", nickname, avatar);
                    }

                    // 对头像地址做规范化处理，避免返回 wxfile://、http://tmp/ 等临时地址
                    avatar = sanitizeAvatar(avatar);
                    
                    ParticipantScoreInfo scoreInfo = new ParticipantScoreInfo(
                        p.getId(),
                        nickname,
                        avatar,
                        p.getTotalScore(),
                        finalScore,
                        p.equals(w), // 是否为获胜者
                        wechatUserId
                    );
                    participantScores.add(scoreInfo);
                    logger.info("添加参与者得分信息: ID={}, 昵称={}, 最终得分={}, 是否获胜={}", 
                               p.getId(), nickname, finalScore, p.equals(w));
                }
                
                // 序列化为JSON
                String totalScoresJson = objectMapper.writeValueAsString(participantScores);
                r.setTotalScores(totalScoresJson);
                logger.info("成功生成参与者得分信息JSON，长度: {}", totalScoresJson.length());
                logger.debug("JSON内容: {}", totalScoresJson);
                
            } catch (Exception ex) {
                logger.error("生成参与者得分信息JSON失败", ex);
                r.setTotalScores("[]"); // 设置空数组作为默认值
            }
            
            logger.info("开始保存MatchResult到数据库...");
            mdao.save(r);
            logger.info("MatchResult保存成功，matchId: {}", r.getMatchId());
            
            logger.info("开始保存Match到数据库...");
            Match savedMatch = dao.save(u);
            logger.info("Match保存成功，matchId: {}", savedMatch.getMatchId());
            
            logger.info("=== 收盘对局完成 ===");
            return savedMatch;
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteMatch(Long id) {
        Optional<Match> matchOpt = dao.findById(id);
        if (!matchOpt.isPresent()) {
            return; // 对局不存在，直接返回
        }
        
        Match match = matchOpt.get();
        Integer matchStatus = match.getStatus();
        
        // 1. 先删除轮次得分记录（因为轮次得分依赖参与者）
        List<RoundScore> roundScores = rdao.findByMatchOrderByRoundNumberAsc(match);
        if (!roundScores.isEmpty()) {
            rdao.deleteAll(roundScores);
        }
        
        // 2. 删除参与者记录
        List<MatchParticipant> participants = pdao.findByMatch(match);
        if (!participants.isEmpty()) {
            pdao.deleteAll(participants);
        }
        
        // 3. 根据对局状态决定是否删除对局结果记录
        if ("已完成".equals(matchStatus)) {
            // 只有已完成的对局才需要删除对局结果记录
            try {
                Optional<MatchResult> resultOpt = mdao.findByMatchId(id);
                if (resultOpt.isPresent()) {
                    // 直接删除记录，避免查询不存在的字段
                    mdao.deleteById(resultOpt.get().getMatchId());
                    logger.info("Deleted match results for completed matchId: {}", id);
                }
            } catch (Exception e) {
                // 忽略删除对局结果时的错误
                logger.warn("Failed to delete match results for completed matchId: {}", id, e);
            }
        } else {
            // 进行中的对局不需要删除对局结果记录（因为还没有创建）
            logger.info("Skipping match results deletion for ongoing matchId: {} with status: {}", id, matchStatus);
        }
        
        // 4. 删除对局结算记录（如果存在）
        // 暂时注释掉，因为数据库中没有match_settlements表
        // try {
        //     sdao.deleteByMatch(match);
        // } catch (Exception e) {
        //     // 忽略删除对局结算时的错误
        //     logger.warn("Failed to delete match settlement for matchId: {}", id, e);
        // }
        
        // 5. 最后删除对局记录
        dao.deleteById(id);
        logger.info("Successfully deleted match with ID: {}", id);
    }

    // 参与者相关方法
    @Override
    @Transactional
    public MatchParticipant addParticipant(Long id, MatchParticipant p) {
        Optional<Match> e = dao.findById(id);
        if (e.isPresent()) {
            Match m = e.get();
            
            // 处理新的API字段结构
            if (p.getWechatUserId() != null && !p.getWechatUserId().trim().isEmpty()) {
                // 新格式：直接使用wechatUserId
                p.setWechatUserId(p.getWechatUserId());
            } else if (p.getUser() != null && p.getUser().getUserId() != null) {
                // 旧格式：从user.userId提取
                p.setWechatUserId(p.getUser().getUserId());
            }
            
            // 处理昵称字段
            if (p.getNickName() != null && !p.getNickName().trim().isEmpty()) {
                // 新格式：使用nickName
                p.setUserName(p.getNickName());
            } else if (p.getUserName() == null || p.getUserName().trim().isEmpty()) {
                throw new IllegalArgumentException("参与者昵称不能为空");
            }
            
            // 处理头像字段
            if (p.getAvatarUrl() != null && !p.getAvatarUrl().trim().isEmpty()) {
                // 新格式：使用 avatarUrl，临时地址会转化为 https 后保存
                String avatar = normalizeAvatarForSave(p.getAvatarUrl());
                if (avatar != null) {
                    p.setAvatar(avatar);
                }
            }
            
            p.setMatch(m);
            
            // 根据 wechatUserId 解析并关联 wechat_users，写入 user_id 与 wechat_user_id，避免表中人员 ID 为空
            String wechatUserIdKey = (p.getWechatUserId() != null && !p.getWechatUserId().trim().isEmpty())
                ? p.getWechatUserId().trim() : null;
            if (wechatUserIdKey == null && p.getUser() != null && p.getUser().getUserId() != null) {
                wechatUserIdKey = p.getUser().getUserId().trim();
            }
            if (wechatUserIdKey != null) {
                WechatUser wechatUser = resolveWechatUser(wechatUserIdKey);
                if (wechatUser != null) {
                    p.setUser(wechatUser);
                    p.setWechatUserId(wechatUser.getId().toString());
                } else {
                    p.setWechatUserId(wechatUserIdKey);
                }
            }
            
            // 唯一约束 (match_id, user_id/wechat_user_id)：若用户已在对局中，直接返回已有参与者，避免重复插入
            wechatUserIdKey = (p.getWechatUserId() != null && !p.getWechatUserId().trim().isEmpty())
                ? p.getWechatUserId().trim() : null;
            if (wechatUserIdKey != null) {
                Optional<MatchParticipant> existingByWechat = pdao.findByMatchAndWechatUserId(m, wechatUserIdKey);
                if (existingByWechat.isPresent()) {
                    logger.info("User already in match {} (wechatUserId: {}), returning existing participant {}", id, wechatUserIdKey, existingByWechat.get().getId());
                    return existingByWechat.get();
                }
            }
            if (p.getUser() != null && p.getUser().getId() != null) {
                Optional<MatchParticipant> existingByUser = pdao.findByMatchAndUser(m, p.getUser());
                if (existingByUser.isPresent()) {
                    logger.info("User already in match {} (userId: {}), returning existing participant {}", id, p.getUser().getId(), existingByUser.get().getId());
                    return existingByUser.get();
                }
            }
            
            MatchParticipant saved = pdao.save(p);
            
            logger.info("Created participant {} in match {} with wechat_user_id: {}, total score: {}", 
                       saved.getId(), id, saved.getWechatUserId(), saved.getTotalScore());
            
            return saved;
        }
        return null;
    }

    @Override
    public List<MatchParticipant> getMatchParticipants(Long id) {
        Optional<Match> match = dao.findById(id);
        if (match.isPresent()) {
            return pdao.findByMatch(match.get());
        }
        return Collections.emptyList();
    }

    @Override
    public MatchParticipant updateParticipant(Long id, MatchParticipant p) {
        Optional<MatchParticipant> e = pdao.findById(id);
        if (!e.isPresent()) {
            return null;
        }

        MatchParticipant u = e.get();

        // 只更新昵称/头像；总分由计分接口维护，替换扫码用户时不得覆盖，否则会丢失原手动添加用户的本局累计分
        if (p.getUserName() != null) {
            u.setUserName(p.getUserName());
        }
        // 不根据请求体更新 totalScore，替换 = 同一参与位继承原总分与轮次得分
        // if (p.getTotalScore() != null) { u.setTotalScore(p.getTotalScore()); }

        // 根据 wechatUserId 同步 / 创建 WechatUser，并标记为非游客
        String wechatUserId = p.getWechatUserId();
        if (wechatUserId == null || wechatUserId.trim().isEmpty()) {
            // 前端可能只传昵称/头像，不传 wechatUserId，则回退到已有的 wechat_user_id
            wechatUserId = u.getWechatUserId();
        }
        String nickname = p.getUserName() != null ? p.getUserName() : u.getUserName();
        String avatar = p.getAvatar() != null ? p.getAvatar() : u.getAvatar();

        if (wechatUserId != null && !wechatUserId.trim().isEmpty() &&
            nickname != null && !nickname.trim().isEmpty()) {
            try {
                WechatUser wechatUser = resolveWechatUser(wechatUserId);
                if (wechatUser == null) {
                    wechatUser = new WechatUser();
                    wechatUser.setUserId(wechatUserId);
                }
                // 昵称、头像按前端传入覆盖（临时头像会转化为 https）
                wechatUser.setNickname(nickname);
                avatar = normalizeAvatarForSave(avatar);
                if (avatar != null && !avatar.trim().isEmpty()) {
                    wechatUser.setAvatar(avatar);
                }
                // 个人信息页保存视为已完成注册，标记为非游客
                wechatUser.setIsVisitor(false);
                wechatUser = wdao.save(wechatUser);

                // 建立参与者与 WechatUser 的关联（DB 外键 match_participants.user_id -> wechat_users.id）
                u.setUser(wechatUser);
                u.setWechatUserId(wechatUser.getId().toString());
            } catch (Exception ex) {
                logger.warn("更新参与者时同步 WechatUser 失败, participantId: {}, wechatUserId: {}", id, wechatUserId, ex);
            }
        }

        return pdao.save(u);
    }

    @Override
    @Transactional
    public MatchParticipant quitMatch(Long id) {
        Optional<MatchParticipant> e = pdao.findById(id);
        if (e.isPresent()) {
            MatchParticipant u = e.get();
            
            // 标记为退出状态，不受任何限制
            u.setIsQuit(true);
            u.setQuitTime(System.currentTimeMillis());
            
            MatchParticipant saved = pdao.save(u);
            logger.info("参与者 {} 已标记为退出状态，退出时间: {}", id, saved.getQuitTime());
            return saved;
        }
        return null;
    }

    @Override
    @Transactional
    public MatchParticipant reactivateParticipant(Long id) {
        Optional<MatchParticipant> e = pdao.findById(id);
        if (e.isPresent()) {
            MatchParticipant u = e.get();
            
            // 重新启用参与者：清除退出状态
            u.setIsQuit(false);
            u.setQuitTime(null);
            
            MatchParticipant saved = pdao.save(u);
            logger.info("参与者 {} 已重新启用", id);
            return saved;
        }
        return null;
    }

    // 批量参与者操作方法
    @Override
    @Transactional
    public List<MatchParticipant> addParticipants(Long matchId, List<MatchParticipant> participants) {
        Optional<Match> matchOpt = dao.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new IllegalArgumentException("对局不存在，matchId: " + matchId);
        }
        
        Match match = matchOpt.get();
        List<MatchParticipant> savedParticipants = new ArrayList<>();
        
        for (MatchParticipant participant : participants) {
            // 处理新的API字段结构
            if (participant.getWechatUserId() != null && !participant.getWechatUserId().trim().isEmpty()) {
                // 新格式：直接使用wechatUserId
                participant.setWechatUserId(participant.getWechatUserId());
            } else if (participant.getUser() != null && participant.getUser().getUserId() != null) {
                // 旧格式：从user.userId提取
                participant.setWechatUserId(participant.getUser().getUserId());
            }
            
            // 处理昵称字段
            if (participant.getNickName() != null && !participant.getNickName().trim().isEmpty()) {
                // 新格式：使用nickName
                participant.setUserName(participant.getNickName());
            } else if (participant.getUserName() == null || participant.getUserName().trim().isEmpty()) {
                throw new IllegalArgumentException("参与者昵称不能为空");
            }
            
            // 处理头像字段
            if (participant.getAvatarUrl() != null && !participant.getAvatarUrl().trim().isEmpty()) {
                // 新格式：使用 avatarUrl，临时地址会转化为 https 后保存
                String avatar = normalizeAvatarForSave(participant.getAvatarUrl());
                if (avatar != null) {
                    participant.setAvatar(avatar);
                }
            }
            
            participant.setMatch(match);
            MatchParticipant saved = pdao.save(participant);
            savedParticipants.add(saved);
            
            logger.info("Created participant {} in match {} with wechat_user_id: {}, total score: {}", 
                       saved.getId(), matchId, saved.getWechatUserId(), saved.getTotalScore());
        }
        
        return savedParticipants;
    }

    @Override
    @Transactional
    public List<MatchParticipant> updateParticipants(List<MatchParticipant> participants) {
        List<MatchParticipant> updatedParticipants = new ArrayList<>();
        
        for (MatchParticipant participant : participants) {
            if (participant.getId() != null) {
                Optional<MatchParticipant> existingOpt = pdao.findById(participant.getId());
                if (existingOpt.isPresent()) {
                    MatchParticipant existing = existingOpt.get();
                    if (participant.getUserName() != null) {
                        existing.setUserName(participant.getUserName());
                    }
                    // 不按请求体覆盖 totalScore，替换扫码用户时保留原参与位的累计分
                    // if (participant.getTotalScore() != null) { existing.setTotalScore(participant.getTotalScore()); }

                    // 批量更新时，同样根据 wechatUserId 同步 / 创建 WechatUser，并标记为非游客
                    String wechatUserId = participant.getWechatUserId();
                    if (wechatUserId == null || wechatUserId.trim().isEmpty()) {
                        wechatUserId = existing.getWechatUserId();
                    }
                    String nickname = participant.getUserName() != null ? participant.getUserName() : existing.getUserName();
                    String avatar = participant.getAvatar() != null ? participant.getAvatar() : existing.getAvatar();

                    if (wechatUserId != null && !wechatUserId.trim().isEmpty() &&
                        nickname != null && !nickname.trim().isEmpty()) {
                        try {
                            WechatUser wechatUser = resolveWechatUser(wechatUserId);
                            if (wechatUser == null) {
                                wechatUser = new WechatUser();
                                wechatUser.setUserId(wechatUserId);
                            }
                            wechatUser.setNickname(nickname);
                            avatar = normalizeAvatarForSave(avatar);
                            if (avatar != null && !avatar.trim().isEmpty()) {
                                wechatUser.setAvatar(avatar);
                            }
                            wechatUser.setIsVisitor(false);
                            wechatUser = wdao.save(wechatUser);

                            existing.setUser(wechatUser);
                            existing.setWechatUserId(wechatUser.getId().toString());
                        } catch (Exception ex) {
                            logger.warn("批量更新参与者时同步 WechatUser 失败, participantId: {}, wechatUserId: {}", participant.getId(), wechatUserId, ex);
                        }
                    }

                    MatchParticipant updated = pdao.save(existing);
                    updatedParticipants.add(updated);
                }
            }
        }
        
        return updatedParticipants;
    }

    @Override
    @Transactional
    public List<MatchParticipant> quitParticipants(List<Long> participantIds) {
        List<MatchParticipant> quitParticipants = new ArrayList<>();
        
        for (Long participantId : participantIds) {
            Optional<MatchParticipant> participantOpt = pdao.findById(participantId);
            if (participantOpt.isPresent()) {
                MatchParticipant participant = participantOpt.get();
                pdao.delete(participant);
                quitParticipants.add(participant);
            }
        }
        
        return quitParticipants;
    }

    @Override
    @Transactional
    public void deleteParticipants(List<Long> participantIds) {
        logger.info("开始批量删除参与者，数量: {}", participantIds.size());
        
        for (Long participantId : participantIds) {
            logger.info("处理删除参与者请求，参与者ID: {}", participantId);
            
            Optional<MatchParticipant> participantOpt = pdao.findById(participantId);
            if (!participantOpt.isPresent()) {
                logger.warn("参与者不存在，跳过删除，参与者ID: {}", participantId);
                continue;
            }
            
            MatchParticipant participant = participantOpt.get();
            Long matchId = participant.getMatch() != null ? participant.getMatch().getMatchId() : null;
            logger.info("找到参与者，参与者ID: {}，对局ID: {}", participantId, matchId);
            
            // 检查该参与者在轮次表中是否有记录
            List<RoundScore> roundScores = rdao.findByParticipantOrderByRoundNumberAsc(participant);
            logger.info("检查参与者轮次记录，参与者ID: {}，找到 {} 条轮次记录", participantId, roundScores.size());
            
            if (!roundScores.isEmpty()) {
                logger.warn("参与者 {} 有 {} 条轮次记录，不允许删除", participantId, roundScores.size());
                throw new IllegalStateException("玩家已有对战记录不可删除！");
            }
            
            // 验证通过，删除参与者
            logger.info("验证通过，开始删除参与者，参与者ID: {}", participantId);
            pdao.deleteById(participantId);
            logger.info("成功删除参与者，参与者ID: {}", participantId);
        }
        
        logger.info("批量删除参与者完成，处理数量: {}", participantIds.size());
    }

    // 批量轮次得分操作方法
    @Override
    @Transactional
    public List<RoundScore> batchCreateRoundScores(List<RoundScore> roundScores) {
        if (roundScores == null || roundScores.isEmpty()) {
            throw new IllegalArgumentException("轮次得分列表不能为空");
        }
        
        // 按对局和轮次分组，检查重复
        Map<Long, Set<Integer>> matchRoundMap = new HashMap<>();
        for (RoundScore score : roundScores) {
            if (score.getMatch() != null && score.getMatch().getMatchId() != null && score.getRoundNumber() != null) {
                Long matchId = score.getMatch().getMatchId();
                Integer roundNumber = score.getRoundNumber();
                
                matchRoundMap.computeIfAbsent(matchId, k -> new HashSet<>()).add(roundNumber);
            }
        }
        
        // 检查每个对局的每个轮次是否已存在
        for (Map.Entry<Long, Set<Integer>> entry : matchRoundMap.entrySet()) {
            Long matchId = entry.getKey();
            Set<Integer> roundNumbers = entry.getValue();
            
            Optional<Match> matchOpt = dao.findById(matchId);
            if (matchOpt.isPresent()) {
                Match match = matchOpt.get();
                for (Integer roundNumber : roundNumbers) {
                    List<RoundScore> existingScores = rdao.findByMatchAndRoundNumberOrderByParticipant_Id(match, roundNumber);
                    if (!existingScores.isEmpty()) {
                        logger.warn("对局 {} 的轮次 {} 已存在，无法重复创建", matchId, roundNumber);
                        throw new IllegalArgumentException("对局 " + matchId + " 的轮次 " + roundNumber + " 已存在，无法重复创建");
                    }
                }
            }
        }
        
        List<RoundScore> savedScores = new ArrayList<>();
        
        for (RoundScore score : roundScores) {
            // 验证必要字段
            if (score.getMatch() == null || score.getParticipant() == null || score.getRoundNumber() == null) {
                throw new IllegalArgumentException("轮次得分必须包含对局、参与者和轮次号信息");
            }
            
            // 根据matchId查找Match对象
            Match match = score.getMatch();
            if (match.getMatchId() != null) {
                Optional<Match> matchOpt = dao.findById(match.getMatchId());
                if (matchOpt.isPresent()) {
                    score.setMatch(matchOpt.get());
                } else {
                    throw new IllegalArgumentException("对局不存在，matchId: " + match.getMatchId());
                }
            }
            
            // 根据participantId查找MatchParticipant对象
            MatchParticipant participant = score.getParticipant();
            if (participant.getId() != null) {
                Optional<MatchParticipant> participantOpt = pdao.findById(participant.getId());
                if (participantOpt.isPresent()) {
                    score.setParticipant(participantOpt.get());
                } else {
                    throw new IllegalArgumentException("参与者不存在，participantId: " + participant.getId());
                }
            }
            
            // 设置轮次时间（如果未设置）
            if (score.getRoundTime() == null) {
                score.setRoundTime(System.currentTimeMillis());
            }
            
            // 计算累计得分
            Integer totalScore = calculateParticipantTotalScore(score.getParticipant().getId());
            score.setCumulativeScore(totalScore + score.getScore());
            
            RoundScore saved = rdao.save(score);
            savedScores.add(saved);
            
            // 更新参与者总分
            MatchParticipant updatedParticipant = score.getParticipant();
            if (updatedParticipant.getMatch() == null) {
                // 确保match字段不为null
                updatedParticipant.setMatch(score.getMatch());
            }
            updatedParticipant.setTotalScore(score.getCumulativeScore());
            pdao.save(updatedParticipant);
        }

        // 批量计分后按对局更新 total_rounds 与状态（与 recordRoundScores 行为一致）
        for (Map.Entry<Long, Set<Integer>> entry : matchRoundMap.entrySet()) {
            Long matchId = entry.getKey();
            int maxRound = entry.getValue().stream().mapToInt(Integer::intValue).max().orElse(0);
            updateMatchTotalRounds(matchId, maxRound);
        }

        return savedScores;
    }

    @Override
    @Transactional
    public List<RoundScore> batchUpdateRoundScores(List<RoundScore> roundScores) {
        if (roundScores == null || roundScores.isEmpty()) {
            throw new IllegalArgumentException("轮次得分列表不能为空");
        }
        
        List<RoundScore> updatedScores = new ArrayList<>();
        
        for (RoundScore score : roundScores) {
            if (score.getId() != null) {
                Optional<RoundScore> existingOpt = rdao.findById(score.getId());
                if (existingOpt.isPresent()) {
                    RoundScore existing = existingOpt.get();
                    
                    // 更新得分
                    if (score.getScore() != null) {
                        existing.setScore(score.getScore());
                    }
                    
                    // 重新计算累计得分
                    Integer totalScore = calculateParticipantTotalScore(existing.getParticipant().getId());
                    existing.setCumulativeScore(totalScore + existing.getScore());
                    
                    // 更新轮次时间
                    if (score.getRoundTime() != null) {
                        existing.setRoundTime(score.getRoundTime());
                    }
                    
                    RoundScore updated = rdao.save(existing);
                    updatedScores.add(updated);
                    
                    // 更新参与者总分
                    MatchParticipant participant = existing.getParticipant();
                    participant.setTotalScore(existing.getCumulativeScore());
                    pdao.save(participant);
                }
            }
        }
        
        return updatedScores;
    }

    @Override
    @Transactional
    public void batchDeleteRoundScores(List<Long> scoreIds) {
        if (scoreIds == null || scoreIds.isEmpty()) {
            throw new IllegalArgumentException("轮次得分ID列表不能为空");
        }
        
        for (Long scoreId : scoreIds) {
            rdao.deleteById(scoreId);
        }
    }

    @Override
    public List<RoundScore> batchGetRoundScores(List<Long> scoreIds) {
        if (scoreIds == null || scoreIds.isEmpty()) {
            throw new IllegalArgumentException("轮次得分ID列表不能为空");
        }
        
        List<RoundScore> scores = new ArrayList<>();
        for (Long scoreId : scoreIds) {
            Optional<RoundScore> scoreOpt = rdao.findById(scoreId);
            if (scoreOpt.isPresent()) {
                scores.add(scoreOpt.get());
            }
        }
        
        return scores;
    }

    // 轮次得分相关方法
    @Override
    @Transactional
    public List<RoundScore> recordRoundScores(Long id, Integer r, List<RoundScore> rs) {
        Optional<Match> e = dao.findById(id);
        if (e.isPresent()) {
            Match m = e.get();
            
            // 检查该对局下是否已经存在该轮次的记录
            List<RoundScore> existingScores = rdao.findByMatchAndRoundNumberOrderByParticipant_Id(m, r);
            if (!existingScores.isEmpty()) {
                logger.warn("对局 {} 的轮次 {} 已存在，无法重复创建", id, r);
                throw new IllegalArgumentException("轮次 " + r + " 已存在，无法重复创建");
            }
            
            List<RoundScore> s = new ArrayList<>();
            for (RoundScore score : rs) {
                score.setMatch(m);
                score.setRoundNumber(r);
                
                // 处理participant字段：如果为null，则根据participantId查找
                MatchParticipant participant = score.getParticipant();
                if (participant == null || participant.getId() == null) {
                    Long participantId = score.getParticipantId();
                    if (participantId != null) {
                        Optional<MatchParticipant> pOpt = pdao.findById(participantId);
                        if (pOpt.isPresent()) {
                            participant = pOpt.get();
                            score.setParticipant(participant);
                        } else {
                            logger.error("参与者不存在: participantId={}", participantId);
                            throw new RuntimeException("参与者不存在: participantId=" + participantId);
                        }
                    } else {
                        logger.error("RoundScore对象既没有participant也没有participantId");
                        throw new RuntimeException("参与者信息不完整");
                    }
                }
                
                // 计算累计分数
                Integer totalScore = calculateParticipantTotalScore(participant.getId());
                score.setCumulativeScore(totalScore + score.getScore());
                
                RoundScore saved = rdao.save(score);
                
                // 更新参与者总分
                participant.setTotalScore(score.getCumulativeScore());
                pdao.save(participant);
                
                s.add(saved);
            }
            
            // 完成计分后，更新matches表的total_rounds字段为当前轮次
            // 使用独立的事务方法确保更新立即提交
            updateMatchTotalRounds(id, r);
            
            return s;
        }
        return Collections.emptyList();
    }

    @Override
    public List<RoundScore> getMatchRounds(Long id) {
        Optional<Match> match = dao.findById(id);
        if (match.isPresent()) {
            return rdao.findByMatchOrderByRoundNumberAsc(match.get());
        }
        return Collections.emptyList();
    }

    @Override
    public List<RoundScore> getParticipantRounds(Long id) {
        Optional<MatchParticipant> participant = pdao.findById(id);
        if (participant.isPresent()) {
            return rdao.findByParticipantOrderByRoundNumberAsc(participant.get());
        }
        return Collections.emptyList();
    }

    @Override
    public List<RoundScore> getRoundDetails(Long id, Integer r) {
        Optional<Match> match = dao.findById(id);
        if (match.isPresent()) {
            return rdao.findByMatchAndRoundNumberOrderByParticipant_Id(match.get(), r);
        }
        return Collections.emptyList();
    }
    
    @Override
    public Integer getCurrentRoundNumber(Long id) {
        Optional<Match> match = dao.findById(id);
        if (match.isPresent()) {
            Integer maxRound = rdao.findMaxRoundNumberByMatch(match.get());
            return maxRound != null ? maxRound : 0;
        }
        return 0;
    }

    @Override
    public Integer calculateParticipantTotalScore(Long id) {
        Optional<MatchParticipant> participant = pdao.findById(id);
        if (participant.isPresent()) {
            List<RoundScore> rs = rdao.findByParticipantOrderByRoundNumberAsc(participant.get());
            return rs.stream().mapToInt(RoundScore::getScore).sum();
        }
        return 0;
    }
    
    // 对局结算相关方法
    @Override
    @Transactional
    public MatchSettlement settleMatch(Long id, Double multiplier, String notes) {
        // 暂时注释掉，因为数据库中没有match_settlements表
        throw new RuntimeException("结算功能暂时不可用，因为数据库中没有match_settlements表");
        
        // Optional<Match> e = dao.findById(id);
        // if (e.isPresent()) {
        //     Match m = e.get();
            
        //     // 检查是否已结算
        //     if (sdao.existsByMatch(m)) {
        //         throw new RuntimeException("对局已结算");
        //     }
            
        //     // 创建结算记录
        //     MatchSettlement settlement = new MatchSettlement();
        //     settlement.setMatch(m);
        //     settlement.setMultiplier(multiplier);
        //     settlement.setSettlementTime(System.currentTimeMillis());
        //     settlement.setNotes(notes);
            
        //     // 更新对局状态和结算倍率
        //     m.setStatus(1); // 1:已完成
        //     m.setSettlementMultiplier(multiplier);
        //     m.setEndTime(System.currentTimeMillis());
            
        //     dao.save(m);
            
        //     // 参与者最终得分 = totalScore * multiplier
        //     // 由于final_score字段已删除，最终得分需要在查询时计算
            
        //     return sdao.save(settlement);
        // }
        // return null;
    }
    
    @Override
    public Optional<MatchSettlement> getMatchSettlement(Long id) {
        // 暂时注释掉，因为数据库中没有match_settlements表
        return Optional.empty();
        
        // Optional<Match> match = dao.findById(id);
        // if (match.isPresent()) {
        //     return sdao.findByMatch(match.get());
        // }
        // return Optional.empty();
    }
    
    @Override
    public boolean isMatchSettled(Long id) {
        // 暂时注释掉，因为数据库中没有match_settlements表
        // 通过检查对局状态来判断是否已结算
        Optional<Match> match = dao.findById(id);
        return match.isPresent() && match.get().getStatus() == 1; // 状态为1表示已完成
        
        // Optional<Match> match = dao.findById(id);
        // return match.isPresent() && sdao.existsByMatch(match.get());
    }
    
    @Override
    public List<MatchParticipant> getParticipantsRanking(Long id) {
        Optional<Match> match = dao.findById(id);
        if (match.isPresent()) {
            List<MatchParticipant> participants = pdao.findByMatch(match.get());
            // 按total_score排序
            participants.sort(Comparator.comparing(MatchParticipant::getTotalScore, Comparator.nullsLast(Comparator.reverseOrder())));
            return participants;
        }
        return Collections.emptyList();
    }
    
    @Override
    public Double calculateParticipantFinalScore(Long id) {
        Optional<Match> match = dao.findById(id);
        Optional<MatchParticipant> participant = pdao.findById(id);
        if (match.isPresent() && participant.isPresent()) {
            Double multiplier = match.get().getSettlementMultiplier();
            if (multiplier != null) {
                return participant.get().getTotalScore() * multiplier;
            }
        }
        return null;
    }

    @Override
    public MatchParticipant getMatchWinner(Long id) {
        Optional<Match> match = dao.findById(id);
        if (match.isPresent()) {
            List<MatchParticipant> ps = pdao.findByMatch(match.get());
            if (ps.isEmpty()) return null;
            
            return ps.stream()
                    .max(Comparator.comparing(MatchParticipant::getTotalScore))
                    .orElse(null);
        }
        return null;
    }
    
    // 记录页面查询方法
    @Override
    public List<MatchStatusQueryResponse> getMatchesByStatus(Integer status, String wechatUserId) {
        // 按创建时间倒序排序，最新的在最前面
        List<Match> matches = dao.findByStatusOrderByCreateTimeDesc(status);
        List<MatchStatusQueryResponse> responses = new ArrayList<>();
        
        for (Match match : matches) {
            MatchStatusQueryResponse response = new MatchStatusQueryResponse();
            
            // 设置对局基本信息
            response.setMatchId(match.getMatchId());
            response.setRoomName(match.getRoomName());
            response.setStartTime(match.getStartTime());
            response.setEndTime(match.getEndTime());
            response.setStatus(match.getStatus());
            response.setTotalRounds(match.getTotalRounds());
            response.setSettlementMultiplier(match.getSettlementMultiplier());
            response.setCreateTime(match.getCreateTime());
            response.setUpdateTime(match.getUpdateTime());
            
            // 获取参与者信息
            List<MatchParticipant> allParticipants = pdao.findByMatch(match);
            
            // 如果指定了wechat_user_id，检查该用户是否参与此对局
            // 同时匹配 wechat_user_id 列与关联的 user.id（wechat_users 主键），兼容历史存 openid 或数字 id 的情况
            if (wechatUserId != null && !wechatUserId.trim().isEmpty()) {
                String wechatUserIdTrim = wechatUserId.trim();
                boolean userParticipated = allParticipants.stream()
                    .anyMatch(p -> {
                        if (wechatUserIdTrim.equals(p.getWechatUserId())) return true;
                        if (p.getUser() != null && p.getUser().getId() != null
                            && wechatUserIdTrim.equals(p.getUser().getId().toString())) return true;
                        return false;
                    });
                if (!userParticipated) {
                    continue;
                }
            }
            
            // 构建参与者摘要数据（返回所有参与者）
            List<MatchStatusQueryResponse.ParticipantSummary> participantSummaries = new ArrayList<>();
            
            for (MatchParticipant participant : allParticipants) {
                MatchStatusQueryResponse.ParticipantSummary summary = new MatchStatusQueryResponse.ParticipantSummary();
                summary.setParticipantId(participant.getId());
                summary.setTotalScore(participant.getTotalScore());
                summary.setWechatUserId(participant.getWechatUserId());
                WechatUser wechatUser = resolveWechatUser(participant.getWechatUserId());
                if (wechatUser != null) {
                    summary.setIsVisitor(wechatUser.getIsVisitor() != null ? wechatUser.getIsVisitor() : false);
                    summary.setNickName(wechatUser.getNickname());
                    summary.setAvatar(wechatUser.getAvatar() != null ? wechatUser.getAvatar() : participant.getAvatar());
                } else {
                    summary.setIsVisitor(true);
                    summary.setNickName(participant.getUserName());
                    summary.setAvatar(participant.getAvatar());
                }
                participantSummaries.add(summary);
            }
            
            response.setParticipants(participantSummaries);
            responses.add(response);
        }
        
        logger.info("Found {} matches with status: {}, wechatUserId: {}", responses.size(), status, wechatUserId);
        return responses;
    }
    
    @Override
    public Optional<MatchResultResponse> getMatchResult(Long matchId) {
        logger.info("获取对局结果数据，matchId: {}", matchId);
        
        try {
            // 查找对局结果
            Optional<MatchResult> resultOpt = mdao.findByMatchId(matchId);
            if (!resultOpt.isPresent()) {
                logger.warn("对局结果不存在，matchId: {}", matchId);
                return Optional.empty();
            }
            
            MatchResult result = resultOpt.get();
            logger.info("找到对局结果，matchId: {}, winnerId: {}", matchId, result.getWinner() != null ? result.getWinner().getId() : "无");
            
            // 构建响应对象
            MatchResultResponse response = new MatchResultResponse();
            response.setMatchId(result.getMatchId());
            response.setWinnerId(result.getWinner() != null ? result.getWinner().getId() : null);
            response.setWinnerNickname(result.getWinner() != null ? result.getWinner().getUserName() : null);
            response.setWinnerAvatar(result.getWinner() != null ? result.getWinner().getAvatar() : null);
            response.setHighestScore(result.getHighestScore());
            response.setLowestScore(result.getLowestScore());
            response.setTotalDuration(result.getTotalDuration());
            response.setTotalScores(result.getTotalScores());
            response.setCompletionTime(result.getCompletionTime());
            response.setCreateTime(result.getCreateTime());
            response.setUpdateTime(result.getUpdateTime());
            
            // 解析totalScores JSON数据
            if (result.getTotalScores() != null && !result.getTotalScores().trim().isEmpty()) {
                try {
                    List<ParticipantScoreInfo> participantScores = objectMapper.readValue(
                        result.getTotalScores(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ParticipantScoreInfo.class)
                    );
                    response.setParticipantScores(participantScores);
                    logger.info("成功解析参与者得分信息，数量: {}", participantScores.size());
                } catch (Exception e) {
                    logger.error("解析totalScores JSON数据失败", e);
                    response.setParticipantScores(new ArrayList<>());
                }
            } else {
                response.setParticipantScores(new ArrayList<>());
            }
            
            logger.info("成功构建对局结果响应，matchId: {}", matchId);
            return Optional.of(response);
            
        } catch (Exception e) {
            logger.error("获取对局结果失败，matchId: {}", matchId, e);
            return Optional.empty();
        }
    }
    
    /**
     * 更新对局的总轮次字段
     * 使用独立的事务（REQUIRES_NEW）确保更新立即提交到数据库
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateMatchTotalRounds(Long matchId, Integer roundNumber) {
        try {
            Optional<Match> matchOpt = dao.findById(matchId);
            if (matchOpt.isPresent()) {
                Match matchToUpdate = matchOpt.get();
                Integer currentTotalRounds = matchToUpdate.getTotalRounds();
                if (roundNumber > currentTotalRounds) {
                    // 使用@Query方法直接执行SQL UPDATE，确保total_rounds字段被更新
                    int updatedRows = dao.updateTotalRounds(matchId, roundNumber);
                    logger.info("已更新对局 {} 的总轮次为: {} (之前为: {}), 影响行数: {}",
                               matchId, roundNumber, currentTotalRounds, updatedRows);

                    if (updatedRows > 0) {
                        logger.info("对局 {} 的total_rounds更新成功，已在独立事务中提交", matchId);
                        // 计分后若为准备状态(2)，则更新为进行中(0)，详情页状态会变为「进行中」
                        dao.setStatusToInProgressIfReady(matchId);
                    } else {
                        logger.error("警告：对局 {} 的total_rounds更新失败，影响行数为0", matchId);
                    }
                } else {
                    logger.debug("对局 {} 的当前轮次 {} 不大于已有总轮次 {}，无需更新", 
                                matchId, roundNumber, currentTotalRounds);
                }
            }
        } catch (Exception e) {
            logger.error("更新对局 {} 的total_rounds失败", matchId, e);
            throw e;
        }
    }
}