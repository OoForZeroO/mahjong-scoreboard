package com.mahjong.service.impl;

import com.mahjong.dto.MonthlyStatisticsResponse;
import com.mahjong.dto.PlayerScoreInfo;
import com.mahjong.model.*;
import com.mahjong.repository.*;
import com.mahjong.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private WechatUserRepository wechatUserRepository;

    @Autowired
    private UserMatchStatsRepository userMatchStatsRepository;

    @Autowired
    private UserMonthlyStatsRepository userMonthlyStatsRepository;

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

            // 3. 从 user_match_stats 按时间范围查询（或单月时优先从 user_monthly_stats 取）
            List<UserMatchStats> myStats = userMatchStatsRepository.findByUserIdAndMatchEndTimeBetweenOrderByMatchEndTimeAsc(
                    id, startTimestamp, endTimestamp);
            logger.info("user_match_stats 记录数：{}", myStats.size());

            int totalMatches;
            int winMatches;
            int loseMatches;
            int totalScore;
            double totalMultiplierScore;
            int winTotalScore;
            double winTotalMultiplierScore;
            int loseTotalScore;
            double loseTotalMultiplierScore;

            if (month != null) {
                Optional<UserMonthlyStats> monthlyOpt = userMonthlyStatsRepository.findByUserIdAndYearMonth(id, year * 100 + month);
                if (monthlyOpt.isPresent()) {
                    UserMonthlyStats ms = monthlyOpt.get();
                    totalMatches = ms.getTotalMatches() != null ? ms.getTotalMatches() : 0;
                    winMatches = ms.getWinMatches() != null ? ms.getWinMatches() : 0;
                    loseMatches = ms.getLoseMatches() != null ? ms.getLoseMatches() : 0;
                    totalScore = ms.getTotalScore() != null ? ms.getTotalScore() : 0;
                    totalMultiplierScore = ms.getTotalMultiplierScore() != null ? ms.getTotalMultiplierScore() : 0.0;
                    winTotalScore = ms.getWinTotalScore() != null ? ms.getWinTotalScore() : 0;
                    winTotalMultiplierScore = ms.getWinTotalMultiplierScore() != null ? ms.getWinTotalMultiplierScore() : 0.0;
                    loseTotalScore = ms.getLoseTotalScore() != null ? ms.getLoseTotalScore() : 0;
                    loseTotalMultiplierScore = ms.getLoseTotalMultiplierScore() != null ? ms.getLoseTotalMultiplierScore() : 0.0;
                } else {
                    double[] out = aggregateFromUserMatchStats(myStats);
                    totalMatches = (int) out[0]; winMatches = (int) out[1]; loseMatches = (int) out[2];
                    totalScore = (int) out[3]; totalMultiplierScore = out[4];
                    winTotalScore = (int) out[5]; winTotalMultiplierScore = out[6];
                    loseTotalScore = (int) out[7]; loseTotalMultiplierScore = out[8];
                }
            } else {
                double[] out = aggregateFromUserMatchStats(myStats);
                totalMatches = (int) out[0]; winMatches = (int) out[1]; loseMatches = (int) out[2];
                totalScore = (int) out[3]; totalMultiplierScore = out[4];
                winTotalScore = (int) out[5]; winTotalMultiplierScore = out[6];
                loseTotalScore = (int) out[7]; loseTotalMultiplierScore = out[8];
            }

            logger.info("统计结果 - 总对局数：{}, 胜场：{}, 负场：{}, 总得分：{}, 总倍率分：{}",
                    totalMatches, winMatches, loseMatches, totalScore, totalMultiplierScore);

            // 4. 最高/最低分玩家：该时间段内同局其他玩家的倍率分汇总
            List<Long> matchIds = new ArrayList<>();
            for (UserMatchStats s : myStats) {
                matchIds.add(s.getMatchId());
            }
            Map<String, PlayerScoreInfo> allPlayersScores = new HashMap<>();
            if (!matchIds.isEmpty()) {
                List<UserMatchStats> allInRange = userMatchStatsRepository.findByMatchIdIn(matchIds);
                for (UserMatchStats s : allInRange) {
                    if (s.getUserId().equals(id)) continue;
                    String uid = s.getUserId().toString();
                    PlayerScoreInfo info = allPlayersScores.get(uid);
                    if (info == null) {
                        info = new PlayerScoreInfo();
                        info.setWechatUserId(uid);
                        WechatUser wu = wechatUserRepository.findById(s.getUserId()).orElse(null);
                        info.setNickname(wu != null ? wu.getNickname() : null);
                        info.setAvatar(wu != null ? wu.getAvatar() : null);
                        info.setTotalScore(s.getTotalScore());
                        info.setTotalMultiplierScore(s.getFinalScore() != null ? s.getFinalScore().doubleValue() : 0.0);
                        allPlayersScores.put(uid, info);
                    } else {
                        info.setTotalScore((info.getTotalScore() != null ? info.getTotalScore() : 0) + (s.getTotalScore() != null ? s.getTotalScore() : 0));
                        info.setTotalMultiplierScore((info.getTotalMultiplierScore() != null ? info.getTotalMultiplierScore() : 0.0) + (s.getFinalScore() != null ? s.getFinalScore().doubleValue() : 0.0));
                    }
                }
            }
            PlayerScoreInfo highestPlayer = null;
            PlayerScoreInfo lowestPlayer = null;
            for (PlayerScoreInfo pi : allPlayersScores.values()) {
                if (highestPlayer == null || (pi.getTotalMultiplierScore() != null && pi.getTotalMultiplierScore() > (highestPlayer.getTotalMultiplierScore() != null ? highestPlayer.getTotalMultiplierScore() : 0)))
                    highestPlayer = pi;
                if (lowestPlayer == null || (pi.getTotalMultiplierScore() != null && (lowestPlayer.getTotalMultiplierScore() == null || pi.getTotalMultiplierScore() < lowestPlayer.getTotalMultiplierScore())))
                    lowestPlayer = pi;
            }

            // 5. 构建响应
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

    /** out: [0]=totalMatches, [1]=winMatches, [2]=loseMatches, [3]=totalScore, [4]=totalMultiplierScore, [5]=winTotalScore, [6]=winTotalMultiplierScore, [7]=loseTotalScore, [8]=loseTotalMultiplierScore */
    private static double[] aggregateFromUserMatchStats(List<UserMatchStats> myStats) {
        double[] out = new double[9];
        for (UserMatchStats s : myStats) {
            out[0]++;
            int ts = s.getTotalScore() != null ? s.getTotalScore() : 0;
            int fs = s.getFinalScore() != null ? s.getFinalScore() : 0;
            out[3] += ts;
            out[4] += fs;
            if (Boolean.TRUE.equals(s.getIsWinner())) {
                out[1]++;
                out[5] += ts;
                out[6] += fs;
            } else {
                out[2]++;
                out[7] += ts;
                out[8] += fs;
            }
        }
        return out;
    }
}

