package com.epam.healenium.service.impl;

import com.epam.healenium.constants.Constants;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.RecordDto.ReportRecord;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final HealingResultRepository resultRepository;

    @Override
    public String initialize() {
        Report report = new Report()
                .setCreatedDate(LocalDateTime.now());
        return reportRepository.save(report).getUid();
    }

    @Override
    public String initialize(String uid) {
        Report report = new Report()
                .setUid(uid)
                .setCreatedDate(LocalDateTime.now());
        return reportRepository.save(report).getUid();
    }

    @Override
    public RecordDto generate(String key) {
        RecordDto result = new RecordDto();
        Optional<Report> optionalReport = reportRepository.findById(key);
        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            result.setTime(report.getCreatedDate().format(DateTimeFormatter.ISO_DATE_TIME));
            report.getRecordWrapper().getRecords().forEach(it -> {
                ReportRecord reportRecord = new ReportRecord();
                reportRecord.setDeclaringClass(Constants.PROXY_POST_METHOD_CLASS_PATH.equals(it.getClassName())
                        ? it.getFailedLocator().getValue()
                        : it.getClassName() + "." + it.getMethodName() + "()");
                reportRecord.setScreenShotPath(it.getScreenShotPath());
                reportRecord.setFailedLocatorType(it.getFailedLocator().getType());
                reportRecord.setFailedLocatorValue(it.getFailedLocator().getValue());
                reportRecord.setHealedLocatorType(it.getHealedLocator().getType());
                reportRecord.setHealedLocatorValue(it.getHealedLocator().getValue());
                Optional<HealingResult> healingResultOptional = resultRepository.findById(it.getHealingResultId());
                reportRecord.setSuccessHealing(healingResultOptional.get().isSuccessHealing());
                reportRecord.setHealingResultId(it.getHealingResultId());
                result.getData().add(reportRecord);
            });
        }
        return result;
    }
}