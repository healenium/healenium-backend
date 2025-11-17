package com.epam.healenium.controller;

import com.epam.healenium.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/healenium/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Get backend logs for a specific time range (for proxy to use)
     * @param startTime Start time for log collection
     * @param endTime End time for log collection
     * @return String containing the backend logs for the time range
     */
    @GetMapping("/time-range")
    public ResponseEntity<String> getBackendLogsForTimeRangeGet(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        log.info("[LOGS] Getting backend logs for time range: {} to {}", startTime, endTime);
        
        try {
            return ResponseEntity.ok(logService.getBackendLogsForTimeRange(
                LocalDateTime.parse(startTime), 
                LocalDateTime.parse(endTime)
            ));
        } catch (Exception e) {
            log.error("[LOGS] Error retrieving backend logs: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }
    
    /**
     * Get backend logs for a specific time range or session ID (for proxy to use) - POST version
     * @param requestBody Map containing startTime, endTime, or sessionId
     * @return String containing the backend logs
     */
    @PostMapping("/time-range")
    public ResponseEntity<String> getBackendLogsForTimeRangePost(
            @RequestBody Map<String, String> requestBody) {
        String sessionId = requestBody.get("sessionId");
        if (sessionId != null && !sessionId.isEmpty()) {
            log.info("[LOGS] Getting backend logs for session ID: {}", sessionId);
            try {
                return ResponseEntity.ok(logService.getBackendLogsForSessionId(sessionId));
            } catch (Exception e) {
                log.error("[LOGS] Error retrieving backend logs for session ID: {}", sessionId, e);
                return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
            }
        }

        String startTimeStr = requestBody.get("startTime");
        String endTimeStr = requestBody.get("endTime");
        
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (startTimeStr != null && endTimeStr != null) {
                try {
                    start = LocalDateTime.parse(startTimeStr, LOG_DATE_FORMAT);
                    end = LocalDateTime.parse(endTimeStr, LOG_DATE_FORMAT);
                } catch (Exception e) {
                    start = LocalDateTime.parse(startTimeStr);
                    end = LocalDateTime.parse(endTimeStr);
                }
            }
            
            if (start == null) {
                return ResponseEntity.badRequest().body("Error: No valid time range or session ID provided");
            }
            
            return ResponseEntity.ok(logService.getBackendLogsForTimeRange(start, end));
        } catch (Exception e) {
            log.error("[LOGS] Error retrieving backend logs: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }
}
