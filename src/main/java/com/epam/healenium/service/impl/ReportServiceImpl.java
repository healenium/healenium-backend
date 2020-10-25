package com.epam.healenium.service.impl;

import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.RecordDto.ReportRecord;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.service.ReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    public String initialize() {
        Report report = new Report()
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
                reportRecord.setDeclaringClass(it.getName());
                reportRecord.setScreenShotPath(it.getScreenShotPath());
                reportRecord.setFailedLocatorType(it.getFailedLocator().getType());
                reportRecord.setFailedLocatorValue(it.getFailedLocator().getValue());
                reportRecord.setHealedLocatorType(it.getHealedLocator().getType());
                reportRecord.setHealedLocatorValue(it.getHealedLocator().getValue());
                result.getData().add(reportRecord);
            });
        }
        return result;
    }
}