package com.epam.healenium.service;

import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.ReportDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportService {

    String initialize();

    String initialize(String uid);

    RecordDto generate(String key);

    RecordDto generate();

    List<HealingResult> getDedicatedInfo(String reportId);

    void createReportRecord(HealingResult result, Healing healing, String sessionId, byte[] screenshot);

    List<ReportDto> getAllReports();

    /**
     * Get all reports with option to filter empty reports
     * 
     * @param hideEmpty if true, returns only reports with healing records
     * @return list of reports
     */
    List<ReportDto> getAllReports(boolean hideEmpty);
    
    /**
     * Get all reports with filtering by date range and empty reports
     * 
     * @param hideEmpty if true, returns only reports with healing records
     * @param startDate optional start date for filtering
     * @param endDate optional end date for filtering
     * @return list of reports
     */
    List<ReportDto> getAllReports(boolean hideEmpty, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get reports grouped by time (day or hour)
     * 
     * @param hideEmpty if true, returns only reports with healing records
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param groupLevel level of grouping (day or hour)
     * @return map of reports grouped by time
     */
    Map<String, List<ReportDto>> getReportsGroupedByTime(boolean hideEmpty, LocalDateTime startDate, LocalDateTime endDate, String groupLevel);
    
    /**
     * Generate aggregated report for date range
     * 
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @return aggregated report
     */
    RecordDto generateAggregatedReport(LocalDateTime startDate, LocalDateTime endDate);

    RecordDto editReport(String id, ReportDto reportUpdateDto);
}
