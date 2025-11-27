package com.mahjong.repository;

import com.mahjong.model.Match;
import com.mahjong.model.MatchParticipant;
import com.mahjong.model.RoundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRecordRepository extends JpaRepository<RoundRecord, Long> {
    List<RoundRecord> findByMatch(Match match);
    List<RoundRecord> findByMatchAndRoundNumber(Match match, Integer roundNumber);
    List<RoundRecord> findByParticipant(MatchParticipant participant);
    List<RoundRecord> findByMatchAndParticipant(Match match, MatchParticipant participant);
    Optional<RoundRecord> findByMatchAndParticipantAndRoundNumber(Match match, MatchParticipant participant, Integer roundNumber);
}