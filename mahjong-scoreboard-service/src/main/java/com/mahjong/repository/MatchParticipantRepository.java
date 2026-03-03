package com.mahjong.repository;

import com.mahjong.model.Match;
import com.mahjong.model.MatchParticipant;
import com.mahjong.model.WechatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    List<MatchParticipant> findByMatch(Match match);
    List<MatchParticipant> findByUser(WechatUser user);
    Optional<MatchParticipant> findByMatchAndUser(Match match, WechatUser user);
    Optional<MatchParticipant> findByMatchAndWechatUserId(Match match, String wechatUserId);
}