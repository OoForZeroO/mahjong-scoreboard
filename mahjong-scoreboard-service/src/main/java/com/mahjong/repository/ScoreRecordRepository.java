package com.mahjong.repository;

import com.mahjong.model.ScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRecordRepository extends JpaRepository<ScoreRecord, Long> {
    List<ScoreRecord> findByUserId(Long userId);
    List<ScoreRecord> findByRoomId(Long roomId);
    List<ScoreRecord> findByStatus(String status);
    List<ScoreRecord> findByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT s FROM ScoreRecord s WHERE s.userId = :userId ORDER BY s.createTime DESC")
    List<ScoreRecord> findRecentRecordsByUserId(Long userId);
}