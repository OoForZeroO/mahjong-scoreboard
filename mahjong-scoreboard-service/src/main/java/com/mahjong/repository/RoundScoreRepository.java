package com.mahjong.repository;

import com.mahjong.model.Match;
import com.mahjong.model.MatchParticipant;
import com.mahjong.model.RoundScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundScoreRepository extends JpaRepository<RoundScore, Long> {
    
    // 根据对局ID查询所有轮次得分记录
    List<RoundScore> findByMatchOrderByRoundNumberAsc(Match match);
    
    // 根据对局ID和轮次号查询该轮的所有得分记录
    List<RoundScore> findByMatchAndRoundNumberOrderByParticipant_Id(Match match, Integer roundNumber);
    
    // 根据参与者查询其所有轮次得分记录
    List<RoundScore> findByParticipantOrderByRoundNumberAsc(MatchParticipant participant);
    
    // 根据对局ID、参与者ID和轮次号查询特定记录
    Optional<RoundScore> findByMatchAndParticipantAndRoundNumber(Match match, MatchParticipant participant, Integer roundNumber);
    
    // 查询指定对局的最大轮次号
    @Query("SELECT MAX(rs.roundNumber) FROM RoundScore rs WHERE rs.match = :match")
    Integer findMaxRoundNumberByMatch(@Param("match") Match match);
    
    // 查询指定对局和参与者的累计得分
    @Query("SELECT SUM(rs.score) FROM RoundScore rs WHERE rs.match = :match AND rs.participant = :participant")
    Integer calculateTotalScoreForParticipant(@Param("match") Match match, @Param("participant") MatchParticipant participant);
    
    // 删除指定对局的所有轮次得分记录
    void deleteByMatch(Match match);
    
    // 删除指定参与者的所有轮次得分记录
    void deleteByParticipant(MatchParticipant participant);
}