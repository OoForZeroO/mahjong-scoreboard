package com.mahjong.repository;

import com.mahjong.model.UserMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMatchStatsRepository extends JpaRepository<UserMatchStats, Long> {

    List<UserMatchStats> findByUserIdAndMatchEndTimeBetweenOrderByMatchEndTimeAsc(
            Long userId, Long startTimestamp, Long endTimestamp);

    List<UserMatchStats> findByMatchIdIn(List<Long> matchIds);

    @Modifying
    @Query("DELETE FROM UserMatchStats u WHERE u.matchId = :matchId")
    void deleteByMatchId(@Param("matchId") Long matchId);
}
