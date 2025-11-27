package com.mahjong.repository;

import com.mahjong.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    Optional<MatchResult> findByMatchId(Long matchId);
    boolean existsByMatchId(Long matchId);
    void deleteByMatchId(Long matchId);
}