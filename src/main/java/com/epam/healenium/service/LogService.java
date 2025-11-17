package com.epam.healenium.service;

import java.time.LocalDateTime;

/**
 * Service for retrieving logs related to specific reports/sessions
 */
public interface LogService {
    
    /**
     * Get backend logs for a specific time range
     * @param startTime Start time for log collection
     * @param endTime End time for log collection
     * @return String containing the backend logs for the time range
     */
    String getBackendLogsForTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get backend logs for a specific session ID
     * @param sessionId The session ID to get logs for
     * @return String containing the backend logs for the session
     */
    String getBackendLogsForSessionId(String sessionId);
    
}
