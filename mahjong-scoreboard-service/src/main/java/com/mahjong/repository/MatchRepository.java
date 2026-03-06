package com.mahjong.repository;

import com.mahjong.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(Integer status);
    List<Match> findByStatusOrderByCreateTimeDesc(Integer status);
    List<Match> findByStartTimeGreaterThanEqual(Long startTime);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE matches SET total_rounds = :rounds WHERE match_id = :matchId", nativeQuery = true)
    int updateTotalRounds(@Param("matchId") Long matchId, @Param("rounds") Integer rounds);

    /** 计分后：若对局为准备状态(2)，则更新为进行中(0) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE matches SET status = 0 WHERE match_id = :matchId AND status = 2", nativeQuery = true)
    int setStatusToInProgressIfReady(@Param("matchId") Long matchId);
    
    // 查询指定时间段内已完成的对局
    @Query("SELECT m FROM Match m WHERE m.status = 1 AND m.endTime >= :startTimestamp AND m.endTime <= :endTimestamp")
    List<Match> findCompletedMatchesByTimeRange(@Param("startTimestamp") Long startTimestamp, @Param("endTimestamp") Long endTimestamp);
    
    // 查询24小时前状态为指定值的对局
    @Query("SELECT m FROM Match m WHERE m.status = :status AND m.createTime < :beforeTime")
    List<Match> findMatchesByStatusAndBeforeTime(@Param("status") Integer status, @Param("beforeTime") Long beforeTime);
}