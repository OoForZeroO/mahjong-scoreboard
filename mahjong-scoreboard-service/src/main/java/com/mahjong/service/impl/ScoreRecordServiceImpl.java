package com.mahjong.service.impl;

import com.mahjong.model.ScoreRecord;
import com.mahjong.repository.ScoreRecordRepository;
import com.mahjong.service.ScoreRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScoreRecordServiceImpl implements ScoreRecordService {

    @Autowired
    private ScoreRecordRepository scoreRecordRepository;

    @Override
    public ScoreRecord createScoreRecord(ScoreRecord scoreRecord) {
        return scoreRecordRepository.save(scoreRecord);
    }

    @Override
    public Optional<ScoreRecord> getScoreRecordById(Long matchId) {
        return scoreRecordRepository.findById(matchId);
    }

    @Override
    public List<ScoreRecord> getScoreRecordsByUserId(Long userId) {
        return scoreRecordRepository.findByUserId(userId);
    }

    @Override
    public List<ScoreRecord> getScoreRecordsByRoomId(Long roomId) {
        return scoreRecordRepository.findByRoomId(roomId);
    }

    @Override
    public List<ScoreRecord> getScoreRecordsByStatus(String status) {
        return scoreRecordRepository.findByStatus(status);
    }

    @Override
    public List<ScoreRecord> getScoreRecordsByUserIdAndStatus(Long userId, String status) {
        return scoreRecordRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<ScoreRecord> getRecentRecordsByUserId(Long userId) {
        return scoreRecordRepository.findRecentRecordsByUserId(userId);
    }

    @Override
    public List<ScoreRecord> getAllScoreRecords() {
        return scoreRecordRepository.findAll();
    }

    @Override
    public ScoreRecord updateScoreRecord(Long matchId, ScoreRecord scoreRecord) {
        return scoreRecordRepository.save(scoreRecord);
    }

    @Override
    public void deleteScoreRecord(Long matchId) {
        scoreRecordRepository.deleteById(matchId);
    }
}
