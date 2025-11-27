package com.mahjong.service.impl;

import com.mahjong.model.Match;
import com.mahjong.model.MatchParticipant;
import com.mahjong.repository.MatchParticipantRepository;
import com.mahjong.repository.MatchRepository;
import com.mahjong.repository.MatchResultRepository;
import com.mahjong.repository.RoundScoreRepository;
import com.mahjong.service.CleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据清理服务实现
 */
@Service
public class CleanupServiceImpl implements CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupServiceImpl.class);

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchParticipantRepository participantRepository;

    @Autowired
    private RoundScoreRepository roundScoreRepository;

    @Autowired
    private MatchResultRepository matchResultRepository;

    /**
     * 定时清理任务：每天0点执行
     * 删除24小时之前状态为2（已取消）的对局数据及其关联的参与者数据
     */
    @Scheduled(cron = "0 0 0 * * *") // 每天0点执行
    @Transactional
    public void cleanupCancelledMatches() {
        try {
            logger.info("开始执行定时清理任务：清理24小时前状态为2的对局数据");

            // 计算24小时前的时间戳
            long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

            // 查询24小时前状态为2的对局（使用数据库查询，避免加载所有数据）
            List<Match> cancelledMatches = matchRepository.findMatchesByStatusAndBeforeTime(2, twentyFourHoursAgo);

            logger.info("找到{}个符合条件的已取消对局", cancelledMatches.size());

            if (cancelledMatches.isEmpty()) {
                logger.info("没有需要清理的对局数据");
                return;
            }

            int deletedMatchesCount = 0;
            int deletedParticipantsCount = 0;
            int deletedRoundScoresCount = 0;
            int deletedMatchResultsCount = 0;

            for (Match match : cancelledMatches) {
                try {
                    // 删除对局的参与者数据
                    List<MatchParticipant> participants = participantRepository.findByMatch(match);
                    for (MatchParticipant participant : participants) {
                        // 先查询轮次得分记录数量（用于统计）
                        List<com.mahjong.model.RoundScore> roundScores = roundScoreRepository.findByParticipantOrderByRoundNumberAsc(participant);
                        deletedRoundScoresCount += roundScores.size();
                        
                        // 删除参与者的轮次得分记录
                        roundScoreRepository.deleteByParticipant(participant);
                        
                        // 删除参与者记录
                        participantRepository.delete(participant);
                        deletedParticipantsCount++;
                    }

                    // 删除对局结果记录（如果存在）
                    boolean hasMatchResult = matchResultRepository.findByMatchId(match.getMatchId()).isPresent();
                    if (hasMatchResult) {
                        matchResultRepository.deleteByMatchId(match.getMatchId());
                        deletedMatchResultsCount++;
                    }

                    // 删除对局记录
                    matchRepository.delete(match);
                    deletedMatchesCount++;

                    logger.debug("已删除对局：matchId={}, createTime={}", 
                                match.getMatchId(), match.getCreateTime());

                } catch (Exception e) {
                    logger.error("删除对局失败：matchId={}", match.getMatchId(), e);
                }
            }

            logger.info("清理任务完成：删除对局{}个，参与者{}个，轮次得分记录{}个，结果记录{}个", 
                       deletedMatchesCount, deletedParticipantsCount, deletedRoundScoresCount, deletedMatchResultsCount);

        } catch (Exception e) {
            logger.error("执行定时清理任务失败", e);
        }
    }
}

