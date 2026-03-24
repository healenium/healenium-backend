package com.epam.healenium.controller;

import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.ReportDto;
import com.epam.healenium.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/healenium/report")
@RequiredArgsConstructor
public class ReportController {

    @Value("${app.url.report}")
    private String reportUrl;

    private final ReportService reportService;

    @GetMapping("/{uid}")
    public ModelAndView get(@PathVariable String uid) {
        log.debug("[Report] Get by Id: {}", uid);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report");
        modelAndView.addObject("dto", reportService.generate(uid));
        return modelAndView;
    }

    @GetMapping()
    public ModelAndView get() {
        log.debug("[Report] Get");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report");
        modelAndView.addObject("dto", reportService.generate());
        return modelAndView;
    }

    @PostMapping("/init")
    public String init() {
        log.debug("[Report] Init Request");
        return reportService.initialize();
    }

    @PostMapping("/init/{uid}")
    public String initById(@PathVariable String uid) {
        log.info("[Report] Init Request. Session Id: {}", uid);
        String key = reportService.initialize(uid);
        return Paths.get(reportUrl, key).toString();
    }

    @PostMapping("/build")
    public String build(@RequestHeader("sessionKey") String key) {
        log.debug("[Report] Build. Session Id: {}", key);
        return Paths.get(reportUrl, key).toString();
    }

    @GetMapping("/all")
    public List<ReportDto> getAllReports(
            @RequestParam(required = false, defaultValue = "false") boolean hideEmpty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("[Report] Get all, hideEmpty: {}", hideEmpty);
        return reportService.getAllReports(hideEmpty, startDate, endDate);
    }

    @GetMapping("/grouped-by-time")
    public Map<String, List<ReportDto>> getReportsGroupedByTime(
            @RequestParam(required = false, defaultValue = "false") boolean hideEmpty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "day") String groupLevel) {
        log.debug("[Report] Get grouped-by-time, hideEmpty: {}", hideEmpty);
        return reportService.getReportsGroupedByTime(hideEmpty, startDate, endDate, groupLevel);
    }

    @GetMapping("/aggregated")
    public RecordDto getAggregatedReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("[Report] Get aggregated {}, {}", startDate, endDate);
        return reportService.generateAggregatedReport(startDate, endDate);
    }

    @GetMapping("/data")
    public RecordDto getRecords() {
        log.debug("[Report] Get data");
        return reportService.generate();
    }

    @GetMapping("/data/{uid}")
    public ResponseEntity<RecordDto> getReport(@PathVariable String uid) {
        log.debug("[Report] Get data by Id: {}", uid);

        if (uid == null || uid.trim().isEmpty()) {
            log.warn("[REPORT] Invalid report UID provided: {}", uid);
            return ResponseEntity.badRequest().build();
        }

        try {
            RecordDto report = reportService.generate(uid);
            if (report == null || report.getId() == null) {
                log.warn("[REPORT] Report not found with UID: {}", uid);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("[REPORT] Error retrieving report with UID: {}", uid, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/data/{uid}")
    public RecordDto editReport(@PathVariable String uid, @RequestBody ReportDto editReportDto) {
        log.debug("[Report] Patch data by Id: {}", uid);
        return reportService.editReport(uid, editReportDto);
    }

}