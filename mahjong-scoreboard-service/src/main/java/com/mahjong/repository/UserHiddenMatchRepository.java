package com.mahjong.repository;

import com.mahjong.model.UserHiddenMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHiddenMatchRepository extends JpaRepository<UserHiddenMatch, Long> {
    boolean existsByUserKeyAndMatchId(String userKey, Long matchId);
    List<UserHiddenMatch> findByUserKey(String userKey);
}
