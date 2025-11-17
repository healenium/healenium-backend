package com.epam.healenium.service.impl;

import com.epam.healenium.service.LogService;
import com.epam.healenium.util.LogFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    @Value("${logging.path:./logs}")
    private String backendLogPath;

    /**
     * Get backend logs for a specific time range
     * @param startTime Start time for log collection
     * @param endTime End time for log collection
     * @return String containing the backend logs for the time range
     */
    @Override
    public String getBackendLogsForTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return LogFileReader.getLogsForTimeRange(getAllLogFiles(), startTime, endTime);
    }

    /**
     * Get backend logs for a specific session ID
     * @param sessionId The session ID to get logs for
     * @return String containing the backend logs for the session
     */
    @Override
    public String getBackendLogsForSessionId(String sessionId) {
        log.info("[LOG SERVICE] Getting backend logs for session ID: {}", sessionId);
        String sessionMarker = "[Report] Init Request. Session Id: " + sessionId;
        
        List<Path> logFiles = getAllLogFiles();
        if (logFiles.isEmpty()) {
            return "No backend log files found";
        }
        Collections.sort(logFiles);
        for (Path logFile : logFiles) {
            try {
                StringBuilder logs = new StringBuilder();
                boolean foundSession = false;
                
                for (String line : LogFileReader.readLogFile(logFile)) {
                    if (!foundSession && line.contains(sessionMarker)) {
                        foundSession = true;
                    }
                    
                    if (foundSession) {
                        logs.append(line).append("\n");
                    }
                }
                
                if (foundSession) {
                    return logs.toString();
                }
            } catch (Exception e) {
                log.error("[LOG SERVICE] Error reading log file: {}", logFile, e);
            }
        }
        
        return "No logs found for session ID: " + sessionId;
    }

    /**
     * Get all log files from the logs directory
     * @return List of log files
     */
    private List<Path> getAllLogFiles() {
        try {
            Path logsDir = Paths.get(backendLogPath);
            if (!Files.exists(logsDir)) {
                log.warn("Logs directory does not exist: {}", logsDir);
                return Collections.emptyList();
            }

            return Files.list(logsDir)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith(".log") || name.endsWith(".log.gz");
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting log files from directory: {}", backendLogPath, e);
            return Collections.emptyList();
        }
    }
}
