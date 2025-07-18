package com.epam.healenium.service.impl;

import com.epam.healenium.constants.Constants;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Vcs;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.elitea.*;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.repository.VcsRepository;
import com.epam.healenium.rest.IntegrationRestService;
import com.epam.healenium.service.EliteaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class EliteaServiceImpl implements EliteaService {

    private final ReportRepository reportRepository;
    private final IntegrationRestService integrationRestService;
    private final HealingResultRepository healingResultRepository;
    private final SelectorRepository selectorRepository;
    private final VcsRepository vcsRepository;

    @Override
    public List<EliteaSelectorDetectionRequestDto> selectorDetectionByReport(String reportId) {
        Vcs vcs = getGitHubCredential();
        String repoName = extractRepoName(vcs);
        Report report = getReportById(reportId);

        return report.getRecordWrapper()
                .getRecords()
                .stream()
                .map(record -> createSelectorDetectionRequest(repoName, vcs.getAccessToken(), record))
                .toList();
    }

    @Override
    public EliteaDto createPullRequest(String reportId) {
        Vcs vcs = getGitHubCredential();
        String repoName = vcs.getRepository();
        Report report = getReportById(reportId);

        List<EliteaHealing> healings = report.getRecordWrapper()
                .getRecords()
                .stream()
                .map(this::createHealingIfValid)
                .filter(Objects::nonNull)
                .toList();

        return new EliteaDto()
                .setHealings(healings)
                .setRepositoryName(repoName)
                .setReportName(report.getName());
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

    @Override
    public void updateGitHubToolkit(VcsDto vcsDto) {
        if (!StringUtils.isEmpty(vcsDto.getRepository())
                || !StringUtils.isEmpty(vcsDto.getBranch())
                || !StringUtils.isEmpty(vcsDto.getAccessToken())) {
            vcsRepository.findAll().stream()
                    .findFirst()
                    .ifPresent(vsc ->
                            integrationRestService.callGetEliteaApplicationDetails(vsc, "5")
                                    .map(jsonNode -> updateEliteaToolkitJson(jsonNode, vsc))
                                    .flatMap(modifiedJson ->
                                            integrationRestService.callUpdateEliteaApplicationDetails(vsc, "5", Mono.just(modifiedJson))
                                    )
                                    .subscribe()

                    );
        }
    }

    private ObjectNode updateEliteaToolkitJson(ObjectNode jsonNode, Vcs vcs) {
        jsonNode.remove("versions");
        jsonNode.put("version", jsonNode.get("version_details"));
        jsonNode.remove("version_details");

        ObjectNode versionNode = (ObjectNode) jsonNode.get("version");
        JsonNode toolsNode = versionNode.get("tools");

        if (toolsNode.isArray() && toolsNode.size() > 0) {
            ObjectNode firstToolNode = (ObjectNode) toolsNode.get(0);
            ObjectNode settingsNode = (ObjectNode) firstToolNode.get("settings");

            settingsNode.put("repository", vcs.getRepository());
            settingsNode.put("base_branch", vcs.getBranch());
            settingsNode.put("active_branch", vcs.getBranch());
            settingsNode.put("access_token", vcs.getAccessToken());
        }
        return jsonNode;
    }

    private Vcs getGitHubCredential() {
        return vcsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new NoSuchElementException("VCS not found"));
    }

    private String extractRepoName(Vcs vcs) {
        return vcs.getRepository().replace("https://github.com/", "");
    }

    private Report getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new NoSuchElementException("Report not found: " + reportId));
    }

    private EliteaSelectorDetectionRequestDto createSelectorDetectionRequest(String repoName, String token, RecordWrapper.Record record) {
        String value = record.getFailedLocator().getValue();
        GitSearchResponseDto responseDto = integrationRestService.callGitHubService(repoName, value, token);
        List<GitSearchItem> validGitFragments = validateFragments(repoName, responseDto, value);

        Integer healingResultId = record.getHealingResultId();
        HealingResult healingResult = healingResultRepository.findById(healingResultId)
                .orElse(null);

        if (healingResult != null) {
            Selector selector = healingResult.getHealing().getSelector();
            handleGitFragmentsAndSave(selector, validGitFragments);
            return createSelectorDetectionDto(selector, record, value, validGitFragments);
        }
        return null;
    }

    private void handleGitFragmentsAndSave(Selector selector, List<GitSearchItem> fragments) {
        if (fragments.size() == 1) {
            selector.setClassName(fragments.get(0).getPath());
            selectorRepository.save(selector);
        }
    }

    private EliteaSelectorDetectionRequestDto createSelectorDetectionDto(Selector selector, RecordWrapper.Record record, String value, List<GitSearchItem> validGitFragments) {
        List<String> paths = validGitFragments.stream()
                .map(GitSearchItem::getPath)
                .toList();

        return new EliteaSelectorDetectionRequestDto()
                .setId(selector.getUid())
                .setLocator(value)
                .setLocatorType(record.getFailedLocator().getType())
                .setPathList(paths);
    }

    private List<GitSearchItem> validateFragments(String repoName, GitSearchResponseDto responseDto, String value) {
        if (responseDto == null) {
            return Collections.emptyList();
        }
        return responseDto.getItems().stream()
                .filter(item -> isFragmentValid(repoName, item, value))
                .toList();
    }

    private boolean isFragmentValid(String repoName, GitSearchItem item, String value) {
        return item.getText_matches().stream()
                .anyMatch(textMatch -> isTextMatchValid(repoName, textMatch, item.getPath(), value));
    }

    private boolean isTextMatchValid(String repoName, TextMatches textMatch, String path, String value) {
        if (textMatch.getMatches().size() > 1) {
            String fileContent = integrationRestService.getFile(repoName, path);
            return containsQuotedSubstring(fileContent, value);
        }
        return containsQuotedSubstring(textMatch.getFragment(), value);
    }

    private boolean containsQuotedSubstring(String fragment, String value) {
        return fragment != null && (fragment.contains("'" + value + "'") || fragment.contains("\"" + value + "\""));
    }

    private EliteaHealing createHealingIfValid(RecordWrapper.Record record) {
        List<HealingResult> healingResults = healingResultRepository.findAllById(Collections.singleton(record.getHealingResultId()));
        if (healingResults.isEmpty() || !healingResults.get(0).isSuccessHealing()) {
            return null;
        }

        Selector selector = healingResults.get(0).getHealing().getSelector();
        return createEliteaHealing(record, selector.getClassName(), record.getFailedLocator().getValue());
    }

    private EliteaHealing createEliteaHealing(RecordWrapper.Record record, String filePath, String value) {
        return new EliteaHealing()
                .setBrokenLocator(createEliteaLocator(record.getFailedLocator(), value))
                .setHealedLocator(createEliteaLocator(record.getHealedLocator(), record.getHealedLocator().getValue()))
                .setFilePath(filePath);
    }

    private static EliteaLocator createEliteaLocator(Locator locator, String value) {
        return new EliteaLocator()
                .setType(locator.getType())
                .setValue(value);
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
}
