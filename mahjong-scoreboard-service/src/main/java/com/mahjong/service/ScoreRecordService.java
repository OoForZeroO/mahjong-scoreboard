package com.mahjong.service;

import com.mahjong.model.ScoreRecord;
import java.util.List;
import java.util.Optional;

public interface ScoreRecordService {
    ScoreRecord createScoreRecord(ScoreRecord scoreRecord);
    Optional<ScoreRecord> getScoreRecordById(Long matchId);
    List<ScoreRecord> getScoreRecordsByUserId(Long userId);
    List<ScoreRecord> getScoreRecordsByRoomId(Long roomId);
    List<ScoreRecord> getScoreRecordsByStatus(String status);
    List<ScoreRecord> getScoreRecordsByUserIdAndStatus(Long userId, String status);
    List<ScoreRecord> getRecentRecordsByUserId(Long userId);
    List<ScoreRecord> getAllScoreRecords();
    ScoreRecord updateScoreRecord(Long matchId, ScoreRecord scoreRecord);
    void deleteScoreRecord(Long matchId);
}