package com.mahjong.service;

import com.mahjong.model.Match;
import com.mahjong.model.MatchDetailResponse;
import com.mahjong.model.MatchParticipant;
import com.mahjong.model.MatchSettlement;
import com.mahjong.model.MatchStatusQueryResponse;
import com.mahjong.model.RoundScore;
import com.mahjong.model.EndMatchRequest;
import com.mahjong.dto.MatchResultResponse;

import java.util.List;
import java.util.Optional;

public interface MatchService {
    // 对局相关方法
    Match createMatch(Match match);
    Optional<Match> getMatchById(Long matchId);
    MatchDetailResponse getMatchDetail(Long matchId);
    List<Match> getAllMatches();
    List<Match> getMatchesByRoomId(Long roomId);
    Match updateMatch(Long matchId, Match match);
    Match endMatch(Long matchId);
    Match endMatch(Long matchId, EndMatchRequest request);
    void deleteMatch(Long matchId);
    
    // 参与者相关方法
    MatchParticipant addParticipant(Long matchId, MatchParticipant participant);
    List<MatchParticipant> addParticipants(Long matchId, List<MatchParticipant> participants);
    List<MatchParticipant> getMatchParticipants(Long matchId);
    MatchParticipant updateParticipant(Long participantId, MatchParticipant participant);
    List<MatchParticipant> updateParticipants(List<MatchParticipant> participants);
    MatchParticipant quitMatch(Long participantId);
    MatchParticipant reactivateParticipant(Long participantId);
    List<MatchParticipant> quitParticipants(List<Long> participantIds);
    void deleteParticipants(List<Long> participantIds);
    
    // 轮次得分相关方法
    List<RoundScore> recordRoundScores(Long matchId, Integer roundNumber, List<RoundScore> roundScores);
    List<RoundScore> getMatchRounds(Long matchId);
    List<RoundScore> getParticipantRounds(Long participantId);
    List<RoundScore> getRoundDetails(Long matchId, Integer roundNumber);
    Integer getCurrentRoundNumber(Long matchId);
    Integer calculateParticipantTotalScore(Long participantId);
    
    // 批量轮次得分相关方法
    List<RoundScore> batchCreateRoundScores(List<RoundScore> roundScores);
    List<RoundScore> batchUpdateRoundScores(List<RoundScore> roundScores);
    void batchDeleteRoundScores(List<Long> scoreIds);
    List<RoundScore> batchGetRoundScores(List<Long> scoreIds);
    
    // 对局结算相关方法
    MatchSettlement settleMatch(Long matchId, Double multiplier, String notes);
    Optional<MatchSettlement> getMatchSettlement(Long matchId);
    boolean isMatchSettled(Long matchId);
    
    // 对局结果相关方法
    Optional<MatchResultResponse> getMatchResult(Long matchId);
    
    // 统计相关方法
    MatchParticipant getMatchWinner(Long matchId);
    List<MatchParticipant> getParticipantsRanking(Long matchId);
    Double calculateParticipantFinalScore(Long participantId);
    
    // 记录页面查询方法
    List<MatchStatusQueryResponse> getMatchesByStatus(Integer status, String wechatUserId);
}