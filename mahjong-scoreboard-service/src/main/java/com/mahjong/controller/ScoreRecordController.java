package com.mahjong.controller;

import com.mahjong.model.ScoreRecord;
import com.mahjong.service.ScoreRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score-records")
public class ScoreRecordController {

    @Autowired
    private ScoreRecordService scoreRecordService;

    // 创建计分记录
    @PostMapping
    public ResponseEntity<ScoreRecord> createScoreRecord(@RequestBody ScoreRecord scoreRecord) {
        ScoreRecord createdRecord = scoreRecordService.createScoreRecord(scoreRecord);
        return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
    }

    // 获取所有计分记录
    @GetMapping
    public ResponseEntity<List<ScoreRecord>> getAllScoreRecords() {
        List<ScoreRecord> records = scoreRecordService.getAllScoreRecords();
        return ResponseEntity.ok(records);
    }

    // 根据对局ID获取计分记录
    @GetMapping("/{matchId}")
    public ResponseEntity<ScoreRecord> getScoreRecordById(@PathVariable Long matchId) {
        return scoreRecordService.getScoreRecordById(matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 根据用户ID获取计分记录
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScoreRecord>> getScoreRecordsByUserId(@PathVariable Long userId) {
        List<ScoreRecord> records = scoreRecordService.getScoreRecordsByUserId(userId);
        return ResponseEntity.ok(records);
    }

    // 根据用户ID获取最近的计分记录
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<ScoreRecord>> getRecentRecordsByUserId(@PathVariable Long userId) {
        List<ScoreRecord> records = scoreRecordService.getRecentRecordsByUserId(userId);
        return ResponseEntity.ok(records);
    }

    // 根据棋牌室ID获取计分记录
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ScoreRecord>> getScoreRecordsByRoomId(@PathVariable Long roomId) {
        List<ScoreRecord> records = scoreRecordService.getScoreRecordsByRoomId(roomId);
        return ResponseEntity.ok(records);
    }

    // 根据状态获取计分记录
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ScoreRecord>> getScoreRecordsByStatus(@PathVariable String status) {
        List<ScoreRecord> records = scoreRecordService.getScoreRecordsByStatus(status);
        return ResponseEntity.ok(records);
    }

    // 根据用户ID和状态获取计分记录
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<ScoreRecord>> getScoreRecordsByUserIdAndStatus(
            @PathVariable Long userId, @PathVariable String status) {
        List<ScoreRecord> records = scoreRecordService.getScoreRecordsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(records);
    }

    // 更新计分记录
    @PutMapping("/{matchId}")
    public ResponseEntity<ScoreRecord> updateScoreRecord(@PathVariable Long matchId, @RequestBody ScoreRecord scoreRecord) {
        ScoreRecord updatedRecord = scoreRecordService.updateScoreRecord(matchId, scoreRecord);
        return ResponseEntity.ok(updatedRecord);
    }

    // 删除计分记录
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteScoreRecord(@PathVariable Long matchId) {
        scoreRecordService.deleteScoreRecord(matchId);
        return ResponseEntity.noContent().build();
    }
}