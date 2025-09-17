package com.epam.healenium.service.impl;

import com.epam.healenium.constants.Constants;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.RecordDto.ReportRecord;
import com.epam.healenium.model.dto.ReportDto;
import com.epam.healenium.model.dto.ReportLinkDto;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.service.ReportService;
import com.epam.healenium.util.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final HealingResultRepository healingResultRepository;

    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Override
    public String initialize() {
        LocalDateTime now = LocalDateTime.now();
        Report report = new Report()
                .setCreatedDate(LocalDateTime.now())
                .setName(now.format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")));
        return reportRepository.saveAndFlush(report).getUid();
    }

    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Override
    public String initialize(String uid) {
        LocalDateTime now = LocalDateTime.now();
        Report report = new Report()
                .setUid(uid)
                .setCreatedDate(now)
                .setName(now.format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")));
        return reportRepository.saveAndFlush(report).getUid();
    }

    @Override
    public RecordDto generate() {
        List<Report> allReports = reportRepository.findAllByOrderByCreatedDateDesc();
        if (allReports.isEmpty()) {
            log.warn("[REPORT] No reports found for generation");
            return createEmptyRecordDto();
        }
        
        String latestReportId = allReports.get(0).getUid();
        return generate(latestReportId);
    }

    @Override
    public RecordDto generate(String key) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("[REPORT] Invalid report key provided: {}", key);
            return createEmptyRecordDto();
        }
        
        List<Report> allReports = reportRepository.findAllByOrderByCreatedDateDesc();
        List<ReportLinkDto> reportLinks = getReportLinks(allReports);
        
        Optional<Report> reportOptional = reportRepository.findById(key);
        if (reportOptional.isEmpty()) {
            log.warn("[REPORT] Report not found with key: {}", key);
            return createEmptyRecordDtoWithLinks(reportLinks);
        }
        
        Report report = reportOptional.get();
        return buildRecordDto(report, reportLinks);
    }

    private RecordDto createEmptyRecordDto() {
        return new RecordDto();
    }

    private RecordDto createEmptyRecordDtoWithLinks(List<ReportLinkDto> reportLinks) {
        RecordDto result = new RecordDto();
        result.setReportLinks(reportLinks);
        return result;
    }

    private RecordDto buildRecordDto(Report report, List<ReportLinkDto> reportLinks) {
        RecordDto result = new RecordDto();
        result.setId(report.getUid());
        result.setReportLinks(reportLinks);
        
        // Mark the current report as active
        reportLinks.stream()
                .filter(r -> report.getUid().equals(r.getId()))
                .findFirst()
                .ifPresent(r -> {
                    r.setExtendClass("active");
                    result.setName("Healing Report " + r.getName());
                });
        
        result.setTime(report.getCreatedDate().format(DateTimeFormatter.ISO_DATE_TIME));
        buildReportRecords(result, report);
        
        return result;
    }

    @Override
    public List<HealingResult> getDedicatedInfo(String reportId) {
        return reportRepository.findById(reportId)
                .map(Report::getRecordWrapper)
                .map(RecordWrapper::getRecords)
                .stream()
                .flatMap(Set::stream)
                .map(record -> healingResultRepository.findById(record.getHealingResultId()))
                .flatMap(Optional::stream)
                .filter(this::isInvalidSelectorClass)
                .toList();
    }

    private List<ReportLinkDto> getReportLinks(List<Report> all) {
        List<ReportLinkDto> reportLinks = all.stream()
                .map(r -> new ReportLinkDto()
                        .setId(r.getUid())
                        .setName(r.getName()))
                .collect(Collectors.toList());
        return reportLinks;
    }

    private void buildReportRecords(RecordDto result, Report report) {
        report.getRecordWrapper().getRecords().forEach(record -> {
            ReportRecord reportRecord = createReportRecord(record);
            result.getData().add(reportRecord);
        });
    }


    /**
     * Create record in report about healing
     *
     * @param result
     * @param healing
     * @param sessionId
     */
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Override
    public void createReportRecord(HealingResult result, Healing healing, String sessionId, byte[] screenshot) {
        if (StringUtils.isEmpty(sessionId)) {
            log.warn("[REPORT] Session ID is empty, cannot create report record");
            return;
        }
        
        if (result == null || healing == null) {
            log.warn("[REPORT] Healing result or healing is null, cannot create report record");
            return;
        }
        
        String screenshotDir = "/screenshots/" + sessionId;
        String screenshotPath = persistScreenshot(screenshot, screenshotDir);
        log.debug("[REPORT] Screenshot Path: {}", screenshotPath);
        
        reportRepository.findById(sessionId).ifPresentOrElse(
                report -> {
                    log.debug("[REPORT] Adding report record. Session ID: {}, Result: {}, Healing: {}", 
                            sessionId, result, healing);
                    report.getRecordWrapper().addRecord(healing, result, screenshotPath);
                    reportRepository.save(report);
                    log.debug("[REPORT] Report record added successfully");
                },
                () -> log.warn("[REPORT] Report not found for session ID: {}", sessionId)
        );
    }

    @Override
    public List<ReportDto> getAllReports() {
        List<Report> all = reportRepository.findAllByOrderByCreatedDateDesc();
        return getAllReportLinks(all);
    }







    @Override
    public RecordDto editReport(String id, ReportDto editReportDto) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("[REPORT] Invalid report ID provided for edit: {}", id);
            return new RecordDto();
        }
        
        if (editReportDto == null || editReportDto.getName() == null || editReportDto.getName().trim().isEmpty()) {
            log.warn("[REPORT] Invalid report data provided for edit");
            return new RecordDto();
        }
        
        return reportRepository.findById(id)
                .map(report -> {
                    log.info("[REPORT] Editing report with ID: {}, new name: {}", id, editReportDto.getName());
                    report.setName(editReportDto.getName());
                    Report savedReport = reportRepository.save(report);
                    
                    RecordDto result = new RecordDto();
                    result.setId(id)
                            .setTime(savedReport.getCreatedDate().format(DateTimeFormatter.ISO_DATE_TIME))
                            .setName(savedReport.getName());
                    buildReportRecords(result, savedReport);
                    return result;
                })
                .orElseGet(() -> {
                    log.warn("[REPORT] Report not found for edit with ID: {}", id);
                    return new RecordDto();
                });
    }

    private List<ReportDto> getAllReportLinks(List<Report> all) {
        return all.stream()
                .map(r -> new ReportDto()
                        .setId(r.getUid())
                        .setName(r.getName() != null
                                ? r.getName()
                                : r.getCreatedDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")))
                        .setDate(r.getCreatedDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"))))
                .collect(Collectors.toList());
    }

    /**
     * @param screenshotContent
     * @param filePath
     */
    private String persistScreenshot(byte[] screenshotContent, String filePath) {
        String rootDir = Paths.get("").toAbsolutePath().toString();
        String fileName = Paths.get(rootDir, filePath, Utils.buildScreenshotName()).toString();
        try {
            File file = new File(fileName);
            FileUtils.writeByteArrayToFile(file, screenshotContent);
        } catch (Exception ex) {
            log.warn("Failed to save screenshot {}", fileName);
        }
        return fileName;
    }

    private ReportRecord createReportRecord(RecordWrapper.Record record) {
        ReportRecord reportRecord = new ReportRecord();
        setDeclaringClass(reportRecord, record);
        setCommonFields(reportRecord, record);
        setHealingResultFields(reportRecord, record.getHealingResultId());
        return reportRecord;
    }

    private void setDeclaringClass(ReportRecord reportRecord, RecordWrapper.Record record) {
        // Try to get class name from selector first (preferred method)
        String className = getClassNameFromSelector(record);
        
        if (className != null && !className.trim().isEmpty()) {
            // Use selector class name if available
            reportRecord.setDeclaringClass(className);
        } else {
            // Fallback to record class name logic
            setDeclaringClassFromRecord(reportRecord, record);
        }
    }

    private String getClassNameFromSelector(RecordWrapper.Record record) {
        return healingResultRepository.findById(record.getHealingResultId())
                .map(healingResult -> healingResult.getHealing().getSelector())
                .map(Selector::getClassName)
                .orElse(null);
    }

    private void setDeclaringClassFromRecord(ReportRecord reportRecord, RecordWrapper.Record record) {
        String declaringClass = record.getClassName().contains(Constants.SINGLE_ELEMENT_PROXY_CLASS_PATH)
                || record.getClassName().contains(Constants.MULTIPLE_ELEMENTS_PROXY_CLASS_PATH)
                ? record.getFailedLocator().getValue()
                : record.getClassName() + "." + record.getMethodName() + "()";
        reportRecord.setDeclaringClass(declaringClass);
    }

    private void setCommonFields(ReportRecord reportRecord, RecordWrapper.Record record) {
        reportRecord.setScreenShotPath(transformPath(record.getScreenShotPath()));
        reportRecord.setFailedLocatorType(record.getFailedLocator().getType());
        reportRecord.setFailedLocatorValue(record.getFailedLocator().getValue());
        reportRecord.setHealedLocatorType(record.getHealedLocator().getType());
        reportRecord.setHealedLocatorValue(record.getHealedLocator().getValue());
        reportRecord.setHealingResultId(record.getHealingResultId());
    }

    private void setHealingResultFields(ReportRecord reportRecord, Integer healingResultId) {
        healingResultRepository.findById(healingResultId)
                .ifPresentOrElse(
                        healingResult -> {
                            reportRecord.setScore(new DecimalFormat("#.###").format(healingResult.getScore()));
                            reportRecord.setSuccessHealing(healingResult.isSuccessHealing());
                        },
                        () -> {
                            log.warn("[REPORT] Could not find healing result with ID: {}", healingResultId);
                            reportRecord.setScore("0.000");
                            reportRecord.setSuccessHealing(false);
                        }
                );
    }

    private String transformPath(String sourcePath) {
        if (sourcePath == null || sourcePath.trim().isEmpty()) {
            log.warn("[REPORT] Source path is null or empty");
            return "";
        }
        
        try {
            int screenshotIndex = sourcePath.lastIndexOf("screenshots");
            if (screenshotIndex == -1) {
                log.warn("[REPORT] 'screenshots' not found in path: {}", sourcePath);
                return sourcePath;
            }
            
            if (screenshotIndex == 0) {
                log.warn("[REPORT] 'screenshots' found at beginning of path: {}", sourcePath);
                return sourcePath;
            }
            
            return sourcePath.substring(screenshotIndex - 1);
        } catch (Exception e) {
            log.warn("[REPORT] Error transforming source path: {}", sourcePath, e);
            return sourcePath;
        }
    }

    private boolean isInvalidSelectorClass(HealingResult healingResult) {
        if (healingResult == null) {
            log.warn("[REPORT] Healing result is null");
            return false;
        }
        
        if (!healingResult.isSuccessHealing()) {
            return false;
        }
        
        try {
            Selector selector = healingResult.getHealing().getSelector();
            if (selector == null) {
                log.warn("[REPORT] Selector is null for healing result ID: {}", healingResult.getId());
                return false;
            }
            
            String className = selector.getClassName();
            if (className == null || className.trim().isEmpty()) {
                log.warn("[REPORT] Class name is null or empty for selector");
                return false;
            }
            
            return className.contains(Constants.SINGLE_ELEMENT_PROXY_CLASS_PATH)
                    || className.contains(Constants.MULTIPLE_ELEMENTS_PROXY_CLASS_PATH);
        } catch (Exception e) {
            log.error("[REPORT] Error checking invalid selector class for healing result ID: {}", 
                    healingResult.getId(), e);
            return false;
        }
    }
}