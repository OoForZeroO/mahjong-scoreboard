package com.mahjong.repository;

import com.mahjong.model.Match;
import com.mahjong.model.MatchSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchSettlementRepository extends JpaRepository<MatchSettlement, Long> {
    
    // 根据对局查询结算记录
    Optional<MatchSettlement> findByMatch(Match match);
    
    // 根据对局ID查询结算记录
    Optional<MatchSettlement> findByMatchMatchId(Long matchId);
    
    // 检查对局是否已有结算记录
    boolean existsByMatch(Match match);
    
    // 删除指定对局的结算记录
    void deleteByMatch(Match match);
}