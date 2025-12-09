package com.mahjong.controller;

import com.mahjong.model.Match;
import com.mahjong.model.MatchDetailResponse;
import com.mahjong.model.MatchParticipant;
import com.mahjong.model.MatchSettlement;
import com.mahjong.model.MatchStatusQueryResponse;
import com.mahjong.model.RoundScore;
import com.mahjong.model.EndMatchRequest;
import com.mahjong.dto.MatchResultResponse;
import com.mahjong.service.MatchService;
import com.mahjong.service.WechatQRCodeService;
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
@RequestMapping("/api/v1/matches")
@CrossOrigin
public class MatchController {

    private static final Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private MatchService matchService;

    @Autowired
    private WechatQRCodeService wechatQRCodeService;

    // 对局相关接口
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMatch(@RequestBody Match match) {
        try {
            // 验证请求参数
            if (match == null) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "请求体不能为空");
            }
            
            // 处理 room 对象：如果提供了 room 对象但没有 roomName，尝试从 room 获取
            if (match.getRoom() != null && (match.getRoomName() == null || match.getRoomName().trim().isEmpty())) {
                if (match.getRoom().getName() != null && !match.getRoom().getName().trim().isEmpty()) {
                    match.setRoomName(match.getRoom().getName());
                }
            }
            
            // roomName现在是可选的，不再验证room对象
            // 如果没有提供roomName，可以设置为默认值
            if (match.getRoomName() == null || match.getRoomName().trim().isEmpty()) {
                match.setRoomName("默认房间");
            }
            
            Match createdMatch = matchService.createMatch(match);
            return ResponseEntity.ok(buildSuccessResponse(createdMatch));
        } catch (IllegalArgumentException e) {
            logger.warn("创建对局参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("创建对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "创建对局失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMatches() {
        try {
            List<Match> matches = matchService.getAllMatches();
            return ResponseEntity.ok(buildSuccessResponse(matches));
        } catch (Exception e) {
            logger.error("获取对局列表失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取对局列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<Map<String, Object>> getMatchById(@PathVariable Long matchId) {
        try {
            Match match = matchService.getMatchById(matchId).orElse(null);
            if (match != null) {
                return ResponseEntity.ok(buildSuccessResponse(match));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
            }
        } catch (Exception e) {
            logger.error("获取对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取对局失败: " + e.getMessage());
        }
    }

    @GetMapping("/{matchId}/detail")
    public ResponseEntity<Map<String, Object>> getMatchDetail(@PathVariable Long matchId) {
        try {
            MatchDetailResponse matchDetail = matchService.getMatchDetail(matchId);
            return ResponseEntity.ok(buildSuccessResponse(matchDetail));
        } catch (IllegalArgumentException e) {
            logger.warn("获取对局详情失败: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("获取对局详情失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取对局详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Map<String, Object>> getMatchesByRoomId(@PathVariable Long roomId) {
        try {
            List<Match> matches = matchService.getMatchesByRoomId(roomId);
            return ResponseEntity.ok(buildSuccessResponse(matches));
        } catch (Exception e) {
            logger.error("获取房间对局列表失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取房间对局列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<Map<String, Object>> updateMatch(@PathVariable Long matchId, @RequestBody Match match) {
        try {
            Match updatedMatch = matchService.updateMatch(matchId, match);
            if (updatedMatch != null) {
                return ResponseEntity.ok(buildSuccessResponse(updatedMatch));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
            }
        } catch (Exception e) {
            logger.error("更新对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "更新对局失败: " + e.getMessage());
        }
    }

    @PutMapping("/{matchId}/end")
    public ResponseEntity<Map<String, Object>> endMatch(@PathVariable Long matchId) {
        try {
            Match endedMatch = matchService.endMatch(matchId);
            if (endedMatch != null) {
                return ResponseEntity.ok(buildSuccessResponse(endedMatch));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
            }
        } catch (Exception e) {
            logger.error("结束对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "结束对局失败: " + e.getMessage());
        }
    }

    @PutMapping("/{matchId}/end-details")
    public ResponseEntity<Map<String, Object>> endMatchWithDetails(@PathVariable Long matchId, @RequestBody EndMatchRequest request) {
        try {
            Match endedMatch = matchService.endMatch(matchId, request);
            if (endedMatch != null) {
                return ResponseEntity.ok(buildSuccessResponse(endedMatch));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
            }
        } catch (Exception e) {
            logger.error("结束对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "结束对局失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Map<String, Object>> deleteMatch(@PathVariable Long matchId) {
        try {
            matchService.deleteMatch(matchId);
            return ResponseEntity.ok(buildSuccessResponse(null));
        } catch (Exception e) {
            logger.error("删除对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "删除对局失败: " + e.getMessage());
        }
    }

    // 参与者相关接口
    @PostMapping("/{matchId}/participants")
    public ResponseEntity<Map<String, Object>> addParticipant(@PathVariable Long matchId, @RequestBody MatchParticipant participant) {
        try {
            MatchParticipant createdParticipant = matchService.addParticipant(matchId, participant);
            if (createdParticipant != null) {
                return ResponseEntity.ok(buildSuccessResponse(createdParticipant));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
            }
        } catch (Exception e) {
            logger.error("添加参与者失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "添加参与者失败: " + e.getMessage());
        }
    }

    @GetMapping("/{matchId}/participants")
    public ResponseEntity<Map<String, Object>> getMatchParticipants(@PathVariable Long matchId) {
        try {
            List<MatchParticipant> participants = matchService.getMatchParticipants(matchId);
            return ResponseEntity.ok(buildSuccessResponse(participants));
        } catch (Exception e) {
            logger.error("获取参与者列表失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/participants/{participantId}")
    public ResponseEntity<Map<String, Object>> updateParticipant(@PathVariable Long participantId, @RequestBody MatchParticipant participant) {
        try {
            MatchParticipant updatedParticipant = matchService.updateParticipant(participantId, participant);
            if (updatedParticipant != null) {
                return ResponseEntity.ok(buildSuccessResponse(updatedParticipant));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "参与者不存在");
            }
        } catch (Exception e) {
            logger.error("更新参与者信息失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "更新参与者信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/participants/{participantId}/quit")
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

    @PutMapping("/participants/{participantId}/reactivate")
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
    
    @PutMapping("/participants/{participantId}/rejoin")
    public ResponseEntity<Map<String, Object>> rejoinMatch(@PathVariable Long participantId) {
        try {
            logger.info("参与者重新加入对局，participantId: {}", participantId);
            MatchParticipant participant = matchService.reactivateParticipant(participantId);
            if (participant != null) {
                logger.info("参与者重新加入成功，participantId: {}", participantId);
                return ResponseEntity.ok(buildSuccessResponse(participant));
            } else {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "参与者不存在");
            }
        } catch (Exception e) {
            logger.error("重新加入对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "重新加入对局失败: " + e.getMessage());
        }
    }

    // 批量参与者接口
    @PostMapping("/{matchId}/participants/batch")
    public ResponseEntity<Map<String, Object>> addParticipants(@PathVariable Long matchId, @RequestBody List<MatchParticipant> participants) {
        try {
            if (participants == null || participants.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "参与者列表不能为空");
            }
            
            List<MatchParticipant> createdParticipants = matchService.addParticipants(matchId, participants);
            return ResponseEntity.ok(buildSuccessResponse(createdParticipants));
        } catch (IllegalArgumentException e) {
            logger.warn("批量添加参与者参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("批量添加参与者失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量添加参与者失败: " + e.getMessage());
        }
    }

    @PutMapping("/participants/batch")
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

    @PutMapping("/participants/batch/quit")
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

    @DeleteMapping("/participants/batch")
    public ResponseEntity<Map<String, Object>> deleteParticipants(@RequestBody List<Long> participantIds) {
        try {
            if (participantIds == null || participantIds.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "参与者ID列表不能为空");
            }
            
            matchService.deleteParticipants(participantIds);
            logger.info("成功批量删除参与者，数量: {}", participantIds.size());
            return ResponseEntity.ok(buildSuccessResponse(null));
        } catch (IllegalStateException e) {
            // 处理业务逻辑错误（如有轮次记录不允许删除）
            // 返回200状态码，但message中说明不允许删除的原因
            logger.warn("删除参与者不允许，原因: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批量删除参与者失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量删除参与者失败: " + e.getMessage());
        }
    }

    // 轮次得分相关接口
    @PostMapping("/{matchId}/rounds/{roundNumber}")
    public ResponseEntity<Map<String, Object>> recordRoundScores(@PathVariable Long matchId, @PathVariable Integer roundNumber, @RequestBody List<RoundScore> roundScores) {
        try {
            List<RoundScore> scores = matchService.recordRoundScores(matchId, roundNumber, roundScores);
            return ResponseEntity.ok(buildSuccessResponse(scores));
        } catch (Exception e) {
            logger.error("记录轮次得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "记录轮次得分失败: " + e.getMessage());
        }
    }

    @GetMapping("/{matchId}/rounds")
    public ResponseEntity<Map<String, Object>> getMatchRounds(@PathVariable Long matchId) {
        try {
            List<RoundScore> rounds = matchService.getMatchRounds(matchId);
            return ResponseEntity.ok(buildSuccessResponse(rounds));
        } catch (Exception e) {
            logger.error("获取对局轮次记录失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取对局轮次记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{matchId}/rounds/current-number")
    public ResponseEntity<Map<String, Object>> getCurrentRoundNumber(@PathVariable Long matchId) {
        try {
            Integer roundNumber = matchService.getCurrentRoundNumber(matchId);
            return ResponseEntity.ok(buildSuccessResponse(roundNumber));
        } catch (Exception e) {
            logger.error("获取当前轮次失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取当前轮次失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{matchId}/rounds/{roundNumber}")
    public ResponseEntity<Map<String, Object>> getRoundDetails(@PathVariable Long matchId, @PathVariable Integer roundNumber) {
        try {
            List<RoundScore> roundDetails = matchService.getRoundDetails(matchId, roundNumber);
            return ResponseEntity.ok(buildSuccessResponse(roundDetails));
        } catch (Exception e) {
            logger.error("获取轮次详情失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取轮次详情失败: " + e.getMessage());
        }
    }

    // 批量轮次得分接口
    @PostMapping("/rounds/batch")
    public ResponseEntity<Map<String, Object>> batchCreateRoundScores(@RequestBody List<RoundScore> roundScores) {
        try {
            if (roundScores == null || roundScores.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "轮次得分列表不能为空");
            }
            
            List<RoundScore> createdScores = matchService.batchCreateRoundScores(roundScores);
            return ResponseEntity.ok(buildSuccessResponse(createdScores));
        } catch (IllegalArgumentException e) {
            logger.warn("批量创建轮次得分参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("批量创建轮次得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量创建轮次得分失败: " + e.getMessage());
        }
    }

    @PutMapping("/rounds/batch")
    public ResponseEntity<Map<String, Object>> batchUpdateRoundScores(@RequestBody List<RoundScore> roundScores) {
        try {
            if (roundScores == null || roundScores.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "轮次得分列表不能为空");
            }
            
            List<RoundScore> updatedScores = matchService.batchUpdateRoundScores(roundScores);
            return ResponseEntity.ok(buildSuccessResponse(updatedScores));
        } catch (IllegalArgumentException e) {
            logger.warn("批量更新轮次得分参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("批量更新轮次得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量更新轮次得分失败: " + e.getMessage());
        }
    }

    @GetMapping("/rounds/batch")
    public ResponseEntity<Map<String, Object>> batchGetRoundScores(@RequestParam List<Long> scoreIds) {
        try {
            if (scoreIds == null || scoreIds.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "轮次得分ID列表不能为空");
            }
            
            List<RoundScore> scores = matchService.batchGetRoundScores(scoreIds);
            return ResponseEntity.ok(buildSuccessResponse(scores));
        } catch (IllegalArgumentException e) {
            logger.warn("批量获取轮次得分参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("批量获取轮次得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量获取轮次得分失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/rounds/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteRoundScores(@RequestBody List<Long> scoreIds) {
        try {
            if (scoreIds == null || scoreIds.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "轮次得分ID列表不能为空");
            }
            
            matchService.batchDeleteRoundScores(scoreIds);
            return ResponseEntity.ok(buildSuccessResponse(null));
        } catch (IllegalArgumentException e) {
            logger.warn("批量删除轮次得分参数错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("批量删除轮次得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量删除轮次得分失败: " + e.getMessage());
        }
    }
    
    // 对局结算相关接口（收盘接口）
    @PostMapping("/{matchId}/settle")
    public ResponseEntity<Map<String, Object>> settleMatch(@PathVariable Long matchId, @RequestBody(required = false) Map<String, Object> settlementData) {
        try {
            logger.info("收到收盘请求，matchId: {}, settlementData: {}", matchId, settlementData);
            
            // 如果有请求体，使用带倍率的收盘接口
            if (settlementData != null && (settlementData.containsKey("multiplier") || settlementData.containsKey("roomName"))) {
                EndMatchRequest request = new EndMatchRequest();
                if (settlementData.containsKey("multiplier")) {
                    Object multiplierObj = settlementData.get("multiplier");
                    if (multiplierObj instanceof Number) {
                        request.setMultiplier(((Number) multiplierObj).doubleValue());
                    }
                }
                if (settlementData.containsKey("roomName")) {
                    request.setRoomName((String) settlementData.get("roomName"));
                }
                
                Match endedMatch = matchService.endMatch(matchId, request);
                if (endedMatch != null) {
                    logger.info("对局收盘成功，matchId: {}", matchId);
                    return ResponseEntity.ok(buildSuccessResponse(endedMatch));
                } else {
                    return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
                }
            } else {
                // 没有请求体或请求体为空，使用简单收盘接口
                Match endedMatch = matchService.endMatch(matchId);
                if (endedMatch != null) {
                    logger.info("对局收盘成功，matchId: {}", matchId);
                    return ResponseEntity.ok(buildSuccessResponse(endedMatch));
                } else {
                    return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
                }
            }
        } catch (RuntimeException e) {
            logger.warn("收盘对局业务错误: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("对局结算失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "对局结算失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{matchId}/result")
    public ResponseEntity<Map<String, Object>> getMatchResult(@PathVariable Long matchId) {
        try {
            logger.info("获取对局结果数据，matchId: {}", matchId);
            
            // 获取对局结果数据（包含结果表所有数据）
            MatchResultResponse result = matchService.getMatchResult(matchId).orElse(null);
            if (result != null) {
                logger.info("成功获取对局结果数据，matchId: {}, winnerId: {}", matchId, result.getWinnerId());
                return ResponseEntity.ok(buildSuccessResponse(result));
            } else {
                logger.warn("对局结果不存在，matchId: {}", matchId);
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局结果不存在");
            }
        } catch (Exception e) {
            logger.error("获取对局结果失败，matchId: {}", matchId, e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取对局结果失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{matchId}/is-completed")
    public ResponseEntity<Map<String, Object>> isMatchCompleted(@PathVariable Long matchId) {
        try {
            boolean isCompleted = matchService.isMatchSettled(matchId);
            return ResponseEntity.ok(buildSuccessResponse(isCompleted));
        } catch (Exception e) {
            logger.error("检查对局完成状态失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "检查对局完成状态失败: " + e.getMessage());
        }
    }
    
    // 统计相关接口
    @GetMapping("/{matchId}/participants/ranking")
    public ResponseEntity<Map<String, Object>> getParticipantsRanking(@PathVariable Long matchId) {
        try {
            List<MatchParticipant> ranking = matchService.getParticipantsRanking(matchId);
            return ResponseEntity.ok(buildSuccessResponse(ranking));
        } catch (Exception e) {
            logger.error("获取参与者排名失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者排名失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/participants/{participantId}/total-score")
    public ResponseEntity<Map<String, Object>> getParticipantTotalScore(@PathVariable Long participantId) {
        try {
            Integer totalScore = matchService.calculateParticipantTotalScore(participantId);
            return ResponseEntity.ok(buildSuccessResponse(totalScore));
        } catch (Exception e) {
            logger.error("获取参与者总分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者总分失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/participants/{participantId}/final-score")
    public ResponseEntity<Map<String, Object>> getParticipantFinalScore(@PathVariable Long participantId) {
        try {
            Double finalScore = matchService.calculateParticipantFinalScore(participantId);
            return ResponseEntity.ok(buildSuccessResponse(finalScore));
        } catch (Exception e) {
            logger.error("获取参与者最终得分失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者最终得分失败: " + e.getMessage());
        }
    }


    @GetMapping("/participants/{participantId}/rounds")
    public ResponseEntity<Map<String, Object>> getParticipantRounds(@PathVariable Long participantId) {
        try {
            List<RoundScore> rounds = matchService.getParticipantRounds(participantId);
            return ResponseEntity.ok(buildSuccessResponse(rounds));
        } catch (Exception e) {
            logger.error("获取参与者轮次记录失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "获取参与者轮次记录失败: " + e.getMessage());
        }
    }

    // 辅助方法
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
    
    // 记录页面查询接口
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getMatchesByStatus(
            @PathVariable Integer status,
            @RequestParam(required = false) String wechat_user_id) {
        try {
            List<MatchStatusQueryResponse> matches = matchService.getMatchesByStatus(status, wechat_user_id);
            return ResponseEntity.ok(buildSuccessResponse(matches));
        } catch (Exception e) {
            logger.error("根据状态查询对局失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "查询对局失败: " + e.getMessage());
        }
    }

    // 二维码生成接口
    @GetMapping("/{matchId}/qrcode")
    public ResponseEntity<Map<String, Object>> getMatchQRCode(@PathVariable Long matchId) {
        try {
            logger.info("获取对局二维码，matchId: {}", matchId);
            
            // 验证对局是否存在
            if (!matchService.getMatchById(matchId).isPresent()) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "对局不存在");
            }
            
            String qrcodeData = wechatQRCodeService.generateMatchQRCode(matchId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("qrcodeData", qrcodeData);
            data.put("matchId", matchId);
            
            logger.info("二维码生成成功，matchId: {}", matchId);
            return ResponseEntity.ok(buildSuccessResponse(data));
        } catch (Exception e) {
            logger.error("生成对局二维码失败", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "生成二维码失败: " + e.getMessage());
        }
    }
}