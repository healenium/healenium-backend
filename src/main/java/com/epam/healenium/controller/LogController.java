package com.epam.healenium.controller;

import com.epam.healenium.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/healenium/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Get backend logs for a specific time range
     * @param startTime Start time for log collection
     * @param endTime End time for log collection
     * @return String containing the backend logs for the time range
     */
    @GetMapping("/time-range")
    public ResponseEntity<String> getBackendLogsForTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start;
            LocalDateTime end;
            
            try {
                start = LocalDateTime.parse(startTime, LOG_DATE_FORMAT);
                end = LocalDateTime.parse(endTime, LOG_DATE_FORMAT);
            } catch (Exception e) {
                log.debug("Failed to parse using LOG_DATE_FORMAT, trying ISO format: {}", e.getMessage());
                try {
                    start = LocalDateTime.parse(startTime);
                    end = LocalDateTime.parse(endTime);
                } catch (Exception e2) {
                    DateTimeFormatter alternateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                    start = LocalDateTime.parse(startTime, alternateFormat);
                    end = LocalDateTime.parse(endTime, alternateFormat);
                }
            }
            
            return ResponseEntity.ok(logService.getBackendLogsForTimeRange(start, end));
        } catch (Exception e) {
            log.error("[LOGS] Error retrieving backend logs: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }
    
    /**
     * Get backend logs for a specific session ID
     * @param sessionId The session ID to get logs for
     * @return String containing the backend logs for the session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<String> getBackendLogsForSession(@PathVariable String sessionId) {
        log.info("[LOGS] Getting backend logs for session ID: {}", sessionId);
        
        try {
            return ResponseEntity.ok(logService.getBackendLogsForSessionId(sessionId));
        } catch (Exception e) {
            log.error("[LOGS] Error retrieving backend logs for session ID: {}", sessionId, e);
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }
}
