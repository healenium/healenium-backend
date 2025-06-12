package com.epam.healenium.service.impl;

import com.epam.healenium.constants.Constants;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.elitea.EliteaDto;
import com.epam.healenium.model.dto.elitea.EliteaHealing;
import com.epam.healenium.model.dto.elitea.EliteaLocator;
import com.epam.healenium.model.dto.elitea.EliteaSelectorDetectionRequestDto;
import com.epam.healenium.model.dto.elitea.GitSearchItem;
import com.epam.healenium.model.dto.elitea.GitSearchResponseDto;
import com.epam.healenium.model.dto.elitea.LocatorPathsDto;
import com.epam.healenium.model.dto.elitea.TextMatches;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class EliteaServiceImpl implements EliteaService {

    private final ReportRepository reportRepository;
    private final GitRestService gitRestService;
    private final HealingResultRepository healingResultRepository;
    private final SelectorRepository selectorRepository;

    //TODO Remove v1
    @Override
    public EliteaDto getEliteaDto(String authorizationHeader, String repoName) {
        Optional<Report> latestReport = reportRepository.findAllByOrderByCreatedDateDesc().stream().findFirst();
        List<EliteaHealing> healings = latestReport
                .map(report -> getEliteaHealings(repoName, authorizationHeader, report))
                .orElse(Collections.emptyList());
        return new EliteaDto()
                .setHealings(healings)
                .setRepositoryName(repoName)
                .setReportName(latestReport.map(Report::getName).orElse(null));
    }

    //TODO Remove v1
    @Override
    public EliteaDto getEliteaDto(String authorizationHeader, String repoName, String reportId) {
        Optional<Report> latestReport = reportRepository.findById(reportId);
        List<EliteaHealing> healings = latestReport
                .map(report -> getEliteaHealings(repoName, authorizationHeader, report))
                .orElse(Collections.emptyList());
        return new EliteaDto()
                .setHealings(healings)
                .setRepositoryName(repoName)
                .setReportName(latestReport.map(Report::getName).orElse(null));
    }

    @Override
    public List<EliteaSelectorDetectionRequestDto> selectorDetectionByReport(String authorizationHeader, String repoName, String reportId) {
        return reportRepository.findById(reportId)
                .map(report -> getEliteaHealings2(repoName, authorizationHeader, report))
                .orElse(List.of());
    }

    @Override
    public EliteaDto getEliteaDto3(String authorizationHeader, String repoName, String reportId) {
        Optional<Report> latestReport = reportRepository.findById(reportId);
        List<EliteaHealing> healings = latestReport
                .map(report -> getEliteaHealings3(repoName, authorizationHeader, report))
                .orElse(Collections.emptyList());
        return new EliteaDto()
                .setHealings(healings)
                .setRepositoryName(repoName)
                .setReportName(latestReport.map(Report::getName).orElse(null));
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

    private boolean isInvalidSelectorClass(HealingResult healingResult) {
        if (!healingResult.isSuccessHealing()) {
            return false;
        }
        Selector selector = healingResult.getHealing().getSelector();
        String className = selector.getClassName();
        return className.contains(Constants.SINGLE_ELEMENT_PROXY_CLASS_PATH)
                || className.contains(Constants.MULTIPLE_ELEMENTS_PROXY_CLASS_PATH);
    }

    private List<EliteaHealing> getEliteaHealings(String repoName, String authorizationHeader, Report report) {
        return report.getRecordWrapper().getRecords().stream()
                .flatMap(record -> createHealingsForRecord(repoName, authorizationHeader, record).stream())
                .collect(Collectors.toList());
    }

    private List<EliteaSelectorDetectionRequestDto> getEliteaHealings2(String repoName, String authorizationHeader, Report report) {
        return report.getRecordWrapper().getRecords().stream()
//                .flatMap(record -> getMultiplePlaceOptions(repoName, authorizationHeader, record).stream())
                .map(record -> getMultiplePlaceOptions(repoName, authorizationHeader, record))
                .collect(Collectors.toList());
    }

    private List<EliteaHealing> getEliteaHealings3(String repoName, String authorizationHeader, Report report) {
        return report.getRecordWrapper().getRecords().stream()
                .map(record -> createHealingsForRecord3(repoName, authorizationHeader, record))
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


    private EliteaSelectorDetectionRequestDto getMultiplePlaceOptions(String repoName, String authorizationHeader, RecordWrapper.Record record) {
        String value = record.getFailedLocator().getValue();
        GitSearchResponseDto responseDto = gitRestService.callExternalService(repoName, value, authorizationHeader);
        List<GitSearchItem> validGitFragments = validateFragment(repoName, responseDto, value);
        Integer healingResultId = record.getHealingResultId();
        Optional<HealingResult> healingResults = healingResultRepository.findById(healingResultId);
        if (healingResults.isPresent()) {
            Selector selector = healingResults.get().getHealing().getSelector();
            if (validGitFragments.size() == 1) {
                selector.setClassName(validGitFragments.get(0).getPath());
                selectorRepository.save(selector);
            } else {
                if (validGitFragments.size() > 1) {
                    List<String> paths = validGitFragments.stream()
                            .map(GitSearchItem::getPath)
                            .toList();
                    return new EliteaSelectorDetectionRequestDto()
                            .setId(selector.getUid())
                            .setLocator(value)
                            .setLocatorType(record.getFailedLocator().getType())
                            .setPathList(paths);

                } else {
                    return new EliteaSelectorDetectionRequestDto()
                            .setId(selector.getUid())
                            .setLocator(value)
                            .setLocatorType(record.getFailedLocator().getType())
                            .setPathList(Collections.emptyList());
                }
            }
        }
//        return Collections.emptyList();
        return null;
    }

    private EliteaHealing createHealingsForRecord3(String repoName, String authorizationHeader, RecordWrapper.Record record) {
        String value = record.getFailedLocator().getValue();
        Integer healingResultId = record.getHealingResultId();
        List<HealingResult> healingResults = healingResultRepository.findAllById(Collections.singleton(healingResultId));
        if (!healingResults.isEmpty() && healingResults.get(0).isSuccessHealing()) {
            Selector selector = healingResults.get(0).getHealing().getSelector();
            selectorRepository.save(selector);
            return createHealing(record, selector.getClassName(), value);
        } else {
            return null;
        }
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
            if (!item.getPath().equals("infra/dump.sql")) {
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
        }
        return resultList;
    }

    private boolean containsQuotedSubstring(String fragment, String value) {
        return fragment != null && (fragment.contains("'" + value + "'") || fragment.contains("\"" + value + "\""));
    }

    @Override
    public void saveLocatorPaths(List<LocatorPathsDto> request) {
        request.forEach(r -> {
            Optional<Selector> selector = selectorRepository.findById(r.getId());
            selector.ifPresent(s -> {
                s.setClassName(r.getSelectedPath());
                selectorRepository.save(s);
            });
        });
    }
}
