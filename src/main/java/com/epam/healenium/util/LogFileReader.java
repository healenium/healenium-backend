package com.epam.healenium.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for reading and processing log files
 */
@Slf4j
public class LogFileReader {
    
    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Read a log file (supporting both plain text and gzipped)
     * @param logFile Path to the log file
     * @return List of strings representing the lines in the file
     * @throws IOException If an I/O error occurs
     */
    public static List<String> readLogFile(Path logFile) throws IOException {
        if (logFile.toString().endsWith(".gz")) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(logFile));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream))) {
                return reader.lines().collect(Collectors.toList());
            }
        } else {
            return Files.readAllLines(logFile);
        }
    }
    
    /**
     * Extract timestamp from a log line
     * @param logLine The log line
     * @return LocalDateTime representing the timestamp, or null if not found
     */
    public static LocalDateTime extractTimestampFromLogLine(String logLine) {
        if (logLine == null || logLine.length() < 23) {
            return null;
        }
        
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3})")
                .matcher(logLine);
            
            if (matcher.find()) {
                return LocalDateTime.parse(matcher.group(1), LOG_DATE_FORMAT);
            }

            return LocalDateTime.parse(logLine.substring(0, 23), LOG_DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get logs for a specific time range from log files
     * @param logFiles List of log files to search
     * @param startTime Start time for log collection
     * @param endTime End time for log collection
     * @return String containing the logs for the time range
     */
    public static String getLogsForTimeRange(List<Path> logFiles, LocalDateTime startTime, LocalDateTime endTime) {
        if (logFiles == null || logFiles.isEmpty()) {
            return "No log files found";
        }

        Collections.sort(logFiles);

        List<LogEntry> allLogEntries = logFiles.stream()
            .flatMap(logFile -> {
                try {
                    return readLogFile(logFile).stream()
                        .map(line -> new AbstractMap.SimpleEntry<>(extractTimestampFromLogLine(line), line))
                        .filter(entry -> entry.getKey() != null && 
                                !entry.getKey().isBefore(startTime) && 
                                !entry.getKey().isAfter(endTime))
                        .map(entry -> new LogEntry(entry.getKey(), entry.getValue()));
                } catch (Exception e) {
                    log.error("Error reading log file: {}", logFile, e);
                    return Stream.empty();
                }
            })
            .toList();
        
        if (allLogEntries.isEmpty()) {
            return "No logs found in the specified time range";
        }

        return allLogEntries.stream()
            .sorted()
            .map(LogEntry::getLogLine)
            .collect(Collectors.joining("\n")) + "\n";
    }
    
    /**
     * Helper class to store log entries with their timestamps for sorting
     */
    public static class LogEntry implements Comparable<LogEntry> {
        private final LocalDateTime timestamp;
        @Getter
        private final String logLine;
        
        public LogEntry(LocalDateTime timestamp, String logLine) {
            this.timestamp = timestamp;
            this.logLine = logLine;
        }

        @Override
        public int compareTo(LogEntry other) {
            return this.timestamp.compareTo(other.timestamp);
        }
    }
}