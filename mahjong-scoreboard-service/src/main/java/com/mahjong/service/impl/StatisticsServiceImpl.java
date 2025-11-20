package com.mahjong.service.impl;

import com.mahjong.dto.MonthlyStatisticsResponse;
import com.mahjong.dto.ParticipantScoreInfo;
import com.mahjong.dto.PlayerScoreInfo;
import com.mahjong.model.*;
import com.mahjong.repository.*;
import com.mahjong.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 统计服务实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Autowired
    private MatchParticipantRepository participantRepository;

    @Autowired
    private WechatUserRepository wechatUserRepository;
    
    @Autowired
    private MatchRepository matchRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MonthlyStatisticsResponse getMonthlyStatistics(String wechatUserId, Integer year, Integer month) {
        try {
            logger.info("开始计算统计，wechatUserId: {}, year: {}, month: {}", wechatUserId, year, month);

            // 1. 获取用户信息
            // 注意：wechatUserId 实际上是数据库中的 id 字段（主键），而不是 user_id 字段
            Long id;
            try {
                id = Long.parseLong(wechatUserId);
            } catch (NumberFormatException e) {
                logger.warn("无效的用户ID格式：{}", wechatUserId);
                // 返回空结构数据
                String yearMonth;
                if (month != null) {
                    yearMonth = String.format("%04d-%02d", year, month);
                } else {
                    yearMonth = String.format("%04d", year);
                }
                MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
                response.setWechatUserId(wechatUserId);
                response.setNickname(null);
                response.setAvatar(null);
                response.setYearMonth(yearMonth);
                response.setTotalScore(0);
                response.setTotalMultiplierScore(0.0);
                response.setTotalMatches(0);
                response.setWinMatches(0);
                response.setLoseMatches(0);
                response.setWinRate(0.0);
                response.setWinTotalScore(0);
                response.setWinTotalMultiplierScore(0.0);
                response.setLoseTotalScore(0);
                response.setLoseTotalMultiplierScore(0.0);
                response.setHighestScorePlayer(null);
                response.setLowestScorePlayer(null);
                return response;
            }
            WechatUser wechatUser = wechatUserRepository.findById(id).orElse(null);
            
            // 2. 计算时间范围
            String yearMonth;
            if (month != null) {
                yearMonth = String.format("%04d-%02d", year, month);
            } else {
                yearMonth = String.format("%04d", year); // 只显示年份
            }
            
            // 如果用户不存在，返回空结构数据
            if (wechatUser == null) {
                logger.warn("未找到微信用户：{}", wechatUserId);
                MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
                response.setWechatUserId(wechatUserId);
                response.setNickname(null);
                response.setAvatar(null);
                response.setYearMonth(yearMonth);
                response.setTotalScore(0);
                response.setTotalMultiplierScore(0.0);
                response.setTotalMatches(0);
                response.setWinMatches(0);
                response.setLoseMatches(0);
                response.setWinRate(0.0);
                response.setWinTotalScore(0);
                response.setWinTotalMultiplierScore(0.0);
                response.setLoseTotalScore(0);
                response.setLoseTotalMultiplierScore(0.0);
                response.setHighestScorePlayer(null);
                response.setLowestScorePlayer(null);
                return response;
            }

            // 计算时间范围
            long startTimestamp;
            long endTimestamp;
            
            if (month != null) {
                // 查询指定月份的数据
                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                
                startTimestamp = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                endTimestamp = monthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
                logger.info("查询月份统计，时间范围：{} - {}", startTimestamp, endTimestamp);
            } else {
                // 查询全年数据
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                
                startTimestamp = yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                endTimestamp = yearEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
                logger.info("查询全年统计，时间范围：{} - {}", startTimestamp, endTimestamp);
            }

            // 3. 查询该用户在该月完成的对局
            List<MatchParticipant> participants = participantRepository.findByUser(wechatUser);
            logger.info("找到参与对局记录数：{}", participants.size());

            // 4. 筛选出该月完成的对局，并统计
            int totalMatches = 0;
            int winMatches = 0;
            int loseMatches = 0;
            int totalScore = 0;
            double totalMultiplierScore = 0.0;
            int winTotalScore = 0;
            double winTotalMultiplierScore = 0.0;
            int loseTotalScore = 0;
            double loseTotalMultiplierScore = 0.0;

            for (MatchParticipant participant : participants) {
                Match match = participant.getMatch();
                if (match == null) {
                    continue;
                }

                // 检查对局是否在该月内完成
                Long endTime = match.getEndTime();
                if (endTime == null || endTime < startTimestamp || endTime > endTimestamp) {
                    continue;
                }

                // 检查对局是否已完成（状态为1）
                if (match.getStatus() == null || match.getStatus() != 1) {
                    continue;
                }

                totalMatches++;

                // 获取倍率
                Double multiplier = match.getSettlementMultiplier();
                if (multiplier == null || multiplier <= 0) {
                    multiplier = 1.0;
                }

                // 计算该对局的总分
                int matchScore = participant.getTotalScore() != null ? participant.getTotalScore() : 0;
                totalScore += matchScore;
                totalMultiplierScore += matchScore * multiplier;

                // 判断胜负（从total_scores JSON中解析，计算最终得分）
                Optional<MatchResult> matchResultOpt = matchResultRepository.findByMatchId(match.getMatchId());
                if (matchResultOpt.isPresent()) {
                    MatchResult matchResult = matchResultOpt.get();
                    try {
                        String totalScoresJson = matchResult.getTotalScores();
                        if (totalScoresJson != null && !totalScoresJson.trim().isEmpty()) {
                            List<ParticipantScoreInfo> scoreInfos = objectMapper.readValue(
                                totalScoresJson, 
                                new TypeReference<List<ParticipantScoreInfo>>() {}
                            );
                            
                            // 找出最高分
                            int maxFinalScore = Integer.MIN_VALUE;
                            for (ParticipantScoreInfo info : scoreInfos) {
                                if (info.getFinalScore() != null && info.getFinalScore() > maxFinalScore) {
                                    maxFinalScore = info.getFinalScore();
                                }
                            }
                            
                            // 判断自己的最终得分是否为最高分
                            int myFinalScore = (int) (matchScore * multiplier);
                            if (myFinalScore >= maxFinalScore) {
                                winMatches++;
                                // 累加胜场得分
                                winTotalScore += matchScore;
                                winTotalMultiplierScore += matchScore * multiplier;
                            } else {
                                loseMatches++;
                                // 累加负场得分
                                loseTotalScore += matchScore;
                                loseTotalMultiplierScore += matchScore * multiplier;
                            }
                        } else {
                            // 如果没有JSON数据，跳过胜负判断
                            logger.warn("对局{}的total_scores为空", match.getMatchId());
                        }
                    } catch (Exception e) {
                        logger.warn("解析total_scores JSON失败", e);
                        // 解析失败，跳过胜负判断
                    }
                } else {
                    // 如果没有match_result记录，跳过胜负判断
                    logger.warn("对局{}没有result记录", match.getMatchId());
                }
            }

            logger.info("统计结果 - 总对局数：{}, 胜场：{}, 负场：{}, 总得分：{}, 总倍率分：{}", 
                       totalMatches, winMatches, loseMatches, totalScore, totalMultiplierScore);

            // 5. 计算该时间段内所有玩家的总得分，找出最高分和最低分玩家
            Map<String, PlayerScoreInfo> allPlayersScores = new HashMap<>(); // key: wechatUserId
            
            // 查询该时间段内所有已完成的对局
            List<Match> completedMatches = matchRepository.findCompletedMatchesByTimeRange(startTimestamp, endTimestamp);
            logger.info("该时间段内已完成的对局数：{}", completedMatches.size());
            
            for (Match match : completedMatches) {
                // 获取对局的倍率
                Double multiplier = match.getSettlementMultiplier();
                if (multiplier == null || multiplier <= 0) {
                    multiplier = 1.0;
                }
                
                // 获取该对局的所有参与者
                List<MatchParticipant> matchParticipants = participantRepository.findByMatch(match);
                
                for (MatchParticipant mp : matchParticipants) {
                    // 排除查询者自身
                    if (mp.getUser() != null && mp.getUser().getId().equals(id)) {
                        continue;
                    }
                    
                    // 使用用户的 id 作为 key
                    String userId = mp.getUser() != null ? mp.getUser().getId().toString() : null;
                    if (userId == null) {
                        continue;
                    }
                    
                    int score = mp.getTotalScore() != null ? mp.getTotalScore() : 0;
                    double multiplierScore = score * multiplier;
                    
                    // 累加该玩家的得分
                    PlayerScoreInfo playerInfo = allPlayersScores.get(userId);
                    if (playerInfo == null) {
                        // 第一次遇到该玩家，创建记录
                        playerInfo = new PlayerScoreInfo();
                        playerInfo.setWechatUserId(userId);
                        playerInfo.setNickname(mp.getUser() != null ? mp.getUser().getNickname() : null);
                        playerInfo.setAvatar(mp.getUser() != null ? mp.getUser().getAvatar() : null);
                        playerInfo.setTotalScore(score);
                        playerInfo.setTotalMultiplierScore(multiplierScore);
                        allPlayersScores.put(userId, playerInfo);
                    } else {
                        // 累加得分
                        playerInfo.setTotalScore(playerInfo.getTotalScore() + score);
                        playerInfo.setTotalMultiplierScore(playerInfo.getTotalMultiplierScore() + multiplierScore);
                    }
                }
            }
            
            logger.info("该时间段内参与对局的玩家数（排除查询者）：{}", allPlayersScores.size());
            
            // 找出最高分和最低分玩家
            PlayerScoreInfo highestPlayer = null;
            PlayerScoreInfo lowestPlayer = null;
            
            for (PlayerScoreInfo playerInfo : allPlayersScores.values()) {
                if (highestPlayer == null || playerInfo.getTotalMultiplierScore() > highestPlayer.getTotalMultiplierScore()) {
                    highestPlayer = playerInfo;
                }
                if (lowestPlayer == null || playerInfo.getTotalMultiplierScore() < lowestPlayer.getTotalMultiplierScore()) {
                    lowestPlayer = playerInfo;
                }
            }
            
            // 6. 构建响应
            MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
            response.setWechatUserId(wechatUserId);
            response.setNickname(wechatUser.getNickname());
            response.setAvatar(wechatUser.getAvatar());
            response.setYearMonth(yearMonth);
            response.setTotalScore(totalScore);
            response.setTotalMultiplierScore(totalMultiplierScore);
            response.setTotalMatches(totalMatches);
            response.setWinMatches(winMatches);
            response.setLoseMatches(loseMatches);
            response.setWinRate(totalMatches > 0 ? (double) winMatches / totalMatches : 0.0);
            response.setWinTotalScore(winTotalScore);
            response.setWinTotalMultiplierScore(winTotalMultiplierScore);
            response.setLoseTotalScore(loseTotalScore);
            response.setLoseTotalMultiplierScore(loseTotalMultiplierScore);
            response.setHighestScorePlayer(highestPlayer);
            response.setLowestScorePlayer(lowestPlayer);

            return response;

        } catch (Exception e) {
            logger.error("计算月度统计失败", e);
            throw new RuntimeException("计算月度统计失败: " + e.getMessage(), e);
        }
    }
}

