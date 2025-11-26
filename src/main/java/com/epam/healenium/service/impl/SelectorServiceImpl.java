package com.epam.healenium.service.impl;


import com.epam.healenium.mapper.SelectorMapper;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.*;
import com.epam.healenium.model.dto.elitea.LocatorPathsDto;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.util.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SelectorServiceImpl implements SelectorService {

    @Value("${app.selector.key.url-for-key}")
    private boolean urlForKey;

    @Value("${app.healing.elements}")
    private boolean findElementsAutoHealing;

    private final SelectorRepository selectorRepository;
    private final SelectorMapper selectorMapper;
    private final HealingRepository healingRepository;
    private final HealingResultRepository healingResultRepository;
    private final ReportRepository reportRepository;

    @Override
    public void saveSelector(SelectorRequestDto request) {
        String id = getSelectorId(request.getLocator(), request.getUrl(), request.getCommand(), urlForKey);
        Optional<Selector> existSelector = selectorRepository.findById(id);
        final Selector selector = selectorMapper.toSelector(request, id, existSelector, findElementsAutoHealing);
        selectorRepository.save(selector);
        log.debug("[Save Elements] Selector: {}", selector);
    }

    @Override
    public ReferenceElementsDto getReferenceElements(RequestDto dto) {
        String selectorId = getSelectorId(dto.getLocator(), dto.getUrl(), dto.getCommand(), urlForKey);
        Optional<Selector> optionalSelector = selectorRepository.findById(selectorId);
        List<List<Node>> paths = optionalSelector
                .map(t -> t.getNodePathWrapper().getNodePath())
                .orElse(Collections.emptyList());
        List<HealingResult> unsuccessfulHealings = healingResultRepository.findUnsuccessfulHealings();
        List<Locator> unsuccessfulLocators = unsuccessfulHealings.stream().filter(
                        healingResult -> healingResult.getHealing().getSelector().getUid().equals(selectorId)
                )
                .map(HealingResult::getLocator)
                .toList();
        return new ReferenceElementsDto()
                .setPaths(paths)
                .setUnsuccessfulLocators(unsuccessfulLocators);
    }

    @Override
    public List<SelectorRequestDto> getAllSelectors() {
        List<Selector> selectors = selectorRepository.findAll();
        return selectorMapper.toRequestDto(selectors);
    }

    @Override
    public ConfigSelectorDto getConfigSelectors() {
        ConfigSelectorDto configSelectorDto = new ConfigSelectorDto();
        List<Selector> disableHealingElement = selectorRepository.findByCommandAndEnableHealing("findElement", false);
        List<Selector> enableHealingElements = selectorRepository.findByCommandAndEnableHealing("findElements", true);
        configSelectorDto
                .setDisableHealingElementDto(selectorMapper.toSelectorDto(disableHealingElement))
                .setEnableHealingElementsDto(selectorMapper.toSelectorDto(enableHealingElements))
                .setUrlForKey(urlForKey)
                .setFindElementsAutoHealing(findElementsAutoHealing);
        return configSelectorDto;
    }

    @Override
    public void setSelectorStatus(SelectorDto dto) {
        selectorRepository.findById(dto.getId())
                .ifPresent(s -> {
                    s.setEnableHealing(dto.isEnableHealing());
                    selectorRepository.save(s);
                });
    }

    @Override
    public void saveSelectorFilePath(RecordDto dto) {
        Report report = reportRepository.findById(dto.getId()).get();
        dto.getData().forEach(d ->
                healingResultRepository.findById(d.getHealingResultId())
                        .ifPresent(healingResult -> {
                            Selector selector = healingResult.getHealing().getSelector();
                            selector.setClassName(d.getDeclaringClass());
                            selectorRepository.saveAndFlush(selector);
                            Optional<RecordWrapper.Record> first = report.getRecordWrapper().getRecords().stream()
                                    .filter(item -> item.getHealingResultId().equals(d.getHealingResultId()))
                                    .findFirst();
                            first.ifPresent(recordWrapper -> recordWrapper
                                    .setClassName(d.getDeclaringClass())
                                    .setMethodName(""));

                        }));
    }

    @Override
    public String getSelectorId(String locator, String url, String command, boolean urlForKey) {
        String addressForKey = Utils.getAddressForKey(url, urlForKey);
        String id = Utils.buildKey(locator, command, addressForKey);
        log.debug("[Selector ID] Locator: {}, URL(source): {}, URL(key): {}, Command: {}, KEY_SELECTOR_URL: {}",
                locator, url, addressForKey, command, urlForKey);
        log.debug("[Selector ID] Result ID: {}", id);
        return id;
    }

    @Override
    public void migrate() {
        List<Selector> all = selectorRepository.findAll();
        migrateSelectors(all);
    }

    @Override
    public void migrateSelectors(List<Selector> sourceSelectors) {
        if (sourceSelectors.isEmpty()) {
            log.debug("[Migrate] There is no selectors to migrate");
            return;
        }
        Map<String, Selector> sourceToTarget = new HashMap<>();
        buildSourceToTargetMap(sourceSelectors, sourceToTarget);
        if (sourceToTarget.isEmpty()) {
            return;
        }
        List<Selector> selectors = selectorRepository.saveAll(sourceToTarget.values());
        log.info("[Migrate] Migrated {} Selectors", selectors.size());
        List<Healing> sourceHealings = healingRepository.findAll();
        migrateHealings(sourceToTarget, selectors, sourceHealings);
        healingRepository.saveAll(sourceHealings);
        log.info("[Migrate] Migrated {} Healings", sourceHealings.size());
        selectorRepository.deleteAll(sourceSelectors);
    }

    @Override
    public void saveLocatorPaths(List<LocatorPathsDto> request, String reportId) {
        if (request == null || request.isEmpty()) {
            log.warn("[ELITEA] Empty locator paths request provided");
            return;
        }

        Optional<Report> reportOptional = reportRepository.findById(reportId);
        if (reportOptional.isEmpty()) {
            log.error("[ELITEA] Report with ID {} not found", reportId);
            throw new IllegalArgumentException("Report not found with ID: " + reportId);
        }

        Report report = reportOptional.get();

        for (LocatorPathsDto locatorPath : request) {
            try {
                Integer healingResultId = Integer.valueOf(locatorPath.getId());
                Optional<HealingResult> healingResult = healingResultRepository.findById(healingResultId);

                if (healingResult.isPresent()) {
                    updateSelectorAndRecord(healingResult.get(), locatorPath, report);
                } else {
                    log.warn("[ELITEA] HealingResult with ID {} not found", healingResultId);
                }
            } catch (NumberFormatException e) {
                log.error("[ELITEA] Invalid healing result ID format: {}", locatorPath.getId());
                throw new IllegalArgumentException("Invalid healing result ID format: " + locatorPath.getId());
            }
        }

        reportRepository.save(report);
        log.info("[ELITEA] Successfully saved {} locator paths for report {}", request.size(), reportId);
    }

    private void migrateHealings(Map<String, Selector> sourceToTarget, List<Selector> selectors, List<Healing> sourceHealings) {
        for (Healing sourceHealing : sourceHealings) {
            String uid = sourceHealing.getSelector().getUid();
            Selector targetSelector = sourceToTarget.get(uid);
            Selector selector = selectors.stream()
                    .filter(s -> s.getUid().equals(targetSelector.getUid()))
                    .findFirst()
                    .orElse(null);
            sourceHealing.setSelector(selector);
        }
    }

    private void buildSourceToTargetMap(List<Selector> sourceSelectors, Map<String, Selector> sourceToTarget) {
        for (Selector sourceSelector : sourceSelectors) {
            Selector targetSelector = selectorMapper.cloneSelector(sourceSelector);
            if (sourceSelector.getCommand() != null) {
                targetSelector.setCommand(sourceSelector.getCommand())
                        .setEnableHealing(sourceSelector.getEnableHealing());
            } else {
                if (sourceSelector.getNodePathWrapper().getNodePath().size() > 1) {
                    targetSelector.setCommand("findElements")
                            .setEnableHealing(false);
                } else {
                    targetSelector.setCommand("findElement")
                            .setEnableHealing(true);
                }
            }
            String selectorId = getSelectorId(sourceSelector.getLocator().getValue(), sourceSelector.getUrl(),
                    targetSelector.getCommand(), urlForKey);
            if (sourceSelector.getUid().equals(selectorId)) {
                continue;
            }
            targetSelector.setUid(selectorId);
            sourceToTarget.put(sourceSelector.getUid(), targetSelector);
        }
    }

    private void updateSelectorAndRecord(HealingResult healingResult, LocatorPathsDto locatorPath, Report report) {
        Selector selector = healingResult.getHealing().getSelector();
        selector.setClassName(locatorPath.getSelectedPath());
        selectorRepository.save(selector);

        Optional<RecordWrapper.Record> recordOptional = report.getRecordWrapper().getRecords().stream()
                .filter(item -> item.getHealingResultId().equals(healingResult.getId()))
                .findFirst();

        recordOptional.ifPresent(record -> {
            record.setClassName(locatorPath.getSelectedPath())
                    .setMethodName("");
        });
    }

}
