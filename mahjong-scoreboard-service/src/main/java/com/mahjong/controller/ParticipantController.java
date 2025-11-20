package com.mahjong.controller;

import com.mahjong.model.MatchParticipant;
import com.mahjong.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantController.class);

    @Autowired
    private MatchService matchService;

    @PutMapping("/{participantId}")
    public ResponseEntity<Map<String, Object>> updateParticipant(@PathVariable Long participantId, @RequestBody MatchParticipant participant) {
        try {
            MatchParticipant updated = matchService.updateParticipant(participantId, participant);
            if (updated != null) {
                return ResponseEntity.ok(buildSuccessResponse(updated));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "参与者不存在");
            }
        } catch (Exception e) {
            logger.error("更新参与者信息失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "更新参与者信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/{participantId}/quit")
    public ResponseEntity<Map<String, Object>> quitMatch(@PathVariable Long participantId) {
        try {
            MatchParticipant participant = matchService.quitMatch(participantId);
            if (participant != null) {
                return ResponseEntity.ok(buildSuccessResponse(participant));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "参与者不存在");
            }
        } catch (Exception e) {
            logger.error("退出对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "退出对局失败: " + e.getMessage());
        }
    }

    @PutMapping("/{participantId}/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateParticipant(@PathVariable Long participantId) {
        try {
            MatchParticipant participant = matchService.reactivateParticipant(participantId);
            if (participant != null) {
                return ResponseEntity.ok(buildSuccessResponse(participant));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "参与者不存在");
            }
        } catch (Exception e) {
            logger.error("重新启用参与者失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "重新启用参与者失败: " + e.getMessage());
        }
    }

    @PutMapping("/batch")
    public ResponseEntity<Map<String, Object>> updateParticipants(@RequestBody List<MatchParticipant> participants) {
        try {
            if (participants == null || participants.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "参与者列表不能为空");
            }
            
            List<MatchParticipant> updatedParticipants = matchService.updateParticipants(participants);
            return ResponseEntity.ok(buildSuccessResponse(updatedParticipants));
        } catch (Exception e) {
            logger.error("批量更新参与者失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量更新参与者失败: " + e.getMessage());
        }
    }

    @PutMapping("/batch/quit")
    public ResponseEntity<Map<String, Object>> quitParticipants(@RequestBody List<Long> participantIds) {
        try {
            if (participantIds == null || participantIds.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "参与者ID列表不能为空");
            }
            
            List<MatchParticipant> quitParticipants = matchService.quitParticipants(participantIds);
            return ResponseEntity.ok(buildSuccessResponse(quitParticipants));
        } catch (Exception e) {
            logger.error("批量退出对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量退出对局失败: " + e.getMessage());
        }
    }

    @GetMapping("/{participantId}/total-score")
    public ResponseEntity<Map<String, Object>> getParticipantTotalScore(@PathVariable Long participantId) {
        try {
            Integer totalScore = matchService.calculateParticipantTotalScore(participantId);
            return ResponseEntity.ok(buildSuccessResponse(totalScore));
        } catch (Exception e) {
            logger.error("获取参与者总分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者总分失败: " + e.getMessage());
        }
    }

    @GetMapping("/{participantId}/final-score")
    public ResponseEntity<Map<String, Object>> getParticipantFinalScore(@PathVariable Long participantId) {
        try {
            Double finalScore = matchService.calculateParticipantFinalScore(participantId);
            return ResponseEntity.ok(buildSuccessResponse(finalScore));
        } catch (Exception e) {
            logger.error("获取参与者最终得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者最终得分失败: " + e.getMessage());
        }
    }

    @GetMapping("/{participantId}/rounds")
    public ResponseEntity<Map<String, Object>> getParticipantRounds(@PathVariable Long participantId) {
        try {
            List<com.mahjong.model.RoundScore> rounds = matchService.getParticipantRounds(participantId);
            return ResponseEntity.ok(buildSuccessResponse(rounds));
        } catch (Exception e) {
            logger.error("获取参与者轮次记录失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者轮次记录失败: " + e.getMessage());
        }
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
}

