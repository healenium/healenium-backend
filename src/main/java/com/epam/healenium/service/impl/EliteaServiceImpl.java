package com.epam.healenium.service.impl;

import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.dto.elitea.EliteaDto;
import com.epam.healenium.model.dto.elitea.EliteaHealing;
import com.epam.healenium.model.dto.elitea.EliteaLocator;
import com.epam.healenium.model.dto.elitea.GitSearchItem;
import com.epam.healenium.model.dto.elitea.GitSearchResponseDto;
import com.epam.healenium.model.dto.elitea.TextMatches;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.rest.GitRestService;
import com.epam.healenium.service.EliteaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class EliteaServiceImpl implements EliteaService {

    private final ReportRepository reportRepository;
    private final GitRestService gitRestService;

    @Override
    public EliteaDto getEliteaDto(String authorizationHeader, String repoName) {
        Optional<Report> latestReport = reportRepository.findAllByOrderByCreatedDateDesc().stream().findFirst();
        List<EliteaHealing> healings = latestReport
                .map(report -> getEliteaHealings(repoName, authorizationHeader, report))
                .orElse(Collections.emptyList());
        return new EliteaDto()
                .setHealings(healings)
                .setRepoName(repoName);
    }

    @Override
    public EliteaDto getEliteaDto(String authorizationHeader, String repoName, String reportId) {
        Optional<Report> latestReport = reportRepository.findById(reportId);
        List<EliteaHealing> healings = latestReport
                .map(report -> getEliteaHealings(repoName, authorizationHeader, report))
                .orElse(Collections.emptyList());
        return new EliteaDto()
                .setHealings(healings)
                .setRepoName(repoName);
    }

    private List<EliteaHealing> getEliteaHealings(String repoName, String authorizationHeader, Report report) {
        return report.getRecordWrapper().getRecords().stream()
                .flatMap(record -> createHealingsForRecord(repoName, authorizationHeader, record).stream())
                .collect(Collectors.toList());
    }

    private List<EliteaHealing> createHealingsForRecord(String repoName, String authorizationHeader, RecordWrapper.Record record) {
        String value = record.getFailedLocator().getValue();
        GitSearchResponseDto responseDto = gitRestService.callExternalService(repoName, value, authorizationHeader);
        List<GitSearchItem> gitSearchItems = validateFragment(repoName, responseDto, value);

        return gitSearchItems.stream()
                .map(GitSearchItem::getPath)
                .map(path -> createHealing(record, path, value))
                .collect(Collectors.toList());
    }

    private EliteaHealing createHealing(RecordWrapper.Record record, String filePath, String value) {
        EliteaLocator brokenLocator = getEliteaLocator(record.getFailedLocator(), value);
        EliteaLocator healedLocator = getEliteaLocator(record.getHealedLocator(), record.getHealedLocator().getValue());
        return new EliteaHealing()
                .setBrokenLocator(brokenLocator)
                .setHealedLocator(healedLocator)
                .setFilePath(filePath);
    }

    private static EliteaLocator getEliteaLocator(Locator record, String value) {
        return new EliteaLocator()
                .setType(record.getType())
                .setValue(value);
    }

    private List<GitSearchItem> validateFragment(String repoName, GitSearchResponseDto responseDto, String value) {
        if (responseDto == null) {
            return new ArrayList<>();
        }
        List<GitSearchItem> resultList = new ArrayList<>();
        for (GitSearchItem item : responseDto.getItems()) {
            for (TextMatches textMatch : item.getText_matches()) {
                if (textMatch.getMatches().size() > 1) {
                    String decode = gitRestService.getFile(repoName, item.getPath());
                    if (containsQuotedSubstring(decode, value)) {
                        resultList.add(item);
                        break;
                    }
                } else {
                    if (containsQuotedSubstring(textMatch.getFragment(), value)) {
                        resultList.add(item);
                        break;
                    }
                }
            }
        }
        return resultList;
    }

    private boolean containsQuotedSubstring(String fragment, String value) {
        return fragment != null && (fragment.contains("'" + value + "'") || fragment.contains("\"" + value + "\""));
    }

}
