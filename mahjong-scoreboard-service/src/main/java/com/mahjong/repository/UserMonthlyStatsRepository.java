package com.mahjong.repository;

import com.mahjong.model.UserMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMonthlyStatsRepository extends JpaRepository<UserMonthlyStats, Long> {

    Optional<UserMonthlyStats> findByUserIdAndYearMonth(Long userId, Integer yearMonth);
}
