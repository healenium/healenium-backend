package com.epam.healenium.service.impl;

import com.epam.healenium.exception.MissingSelectorException;
import com.epam.healenium.mapper.HealingMapper;
import com.epam.healenium.mapper.SelectorMapper;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.HealingDto;
import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.LastHealingDataDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.AmazonS3Service;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.service.PrometheusService;
import com.epam.healenium.specification.HealingSpecBuilder;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.util.StreamUtils;
import com.epam.healenium.util.Utils;
import io.prometheus.client.Summary;
import jdk.internal.joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.io.FileHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.healenium.constants.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HealingServiceImpl implements HealingService {

    private final HealingRepository healingRepository;
    private final SelectorRepository selectorRepository;
    private final HealingResultRepository resultRepository;
    private final ReportRepository reportRepository;

    private final SelectorMapper selectorMapper;
    private final HealingMapper healingMapper;

    private final PrometheusService prometheusService;
    private final AmazonS3Service amazonS3Service;

    @Override
    public void saveSelector(SelectorRequestDto request) {
        final Selector selector = selectorMapper.dtoToDocument(request);
        selectorRepository.save(selector);
    }

    @Override
    public LastHealingDataDto getSelectorPath(RequestDto dto) {
        String selectorId = Utils.buildKey(dto.getClassName(), dto.getMethodName(), dto.getLocator());
        List<Healing> lastHealing = healingRepository.findLastBySelectorId(selectorId, PageRequest.of(0, 1));
        List<List<Node>> paths = selectorRepository.findById(selectorId)
                .map(t -> t.getNodePathWrapper().getNodePath())
                .orElse(Collections.emptyList());
        return new LastHealingDataDto()
                .setPaths(paths)
                .setPageContent(lastHealing.isEmpty() ? Strings.EMPTY : lastHealing.get(0).getPageContent());
    }

    @Override
    public void saveHealing(HealingRequestDto dto, MultipartFile screenshot, Map<String, String> headers, String metrics) {
        // obtain healing
        Healing healing = getHealing(dto);
        // collect healing results
        Collection<HealingResult> healingResults = buildHealingResults(dto.getResults(), healing);
        HealingResult selectedResult = healingResults.stream()
                .filter(it -> {
                    String firstLocator, secondLocator;
                    firstLocator = it.getLocator().getValue();
                    secondLocator = dto.getUsedResult().getLocator().getValue();
                    return firstLocator.equals(secondLocator);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Internal exception! Somehow we lost selected healing result on save"));
        // add report record
        createReportRecord(selectedResult, healing, headers.get(SESSION_KEY), screenshot);
        pushMetrics(metrics, headers, selectedResult);
    }

    @Override
    public Set<HealingDto> getHealings(RequestDto dto) {
        Set<HealingDto> result = new HashSet<>();
        healingRepository.findAll(HealingSpecBuilder.buildSpec(dto)).stream()
                .collect(Collectors.groupingBy(Healing::getSelector))
                .forEach((selector, healingList) -> {
                    // collect healing results
                    Set<HealingResultDto> healingResults = healingList.stream()
                            .flatMap(it -> it.getResults().stream())
                            .sorted(Comparator.comparing(HealingResult::getScore, Comparator.reverseOrder()))
                            .filter(StreamUtils.distinctByKey(HealingResult::getLocator))
                            .map(healingMapper::modelToResultDto)
                            .collect(Collectors.toSet());
                    // build healing dto
                    HealingDto healingDto = new HealingDto()
                            .setClassName(selector.getClassName())
                            .setMethodName(selector.getMethodName())
                            .setLocator(selector.getLocator().getValue())
                            .setResults(healingResults);
                    // add dto to result collection
                    result.add(healingDto);
                });
        return result;
    }

    @Override
    public Set<HealingResultDto> getHealingResults(RequestDto dto) {
        String selectorId = Utils.buildKey(dto.getClassName(), dto.getMethodName(), dto.getLocator());
        return healingRepository.findBySelectorId(selectorId).stream()
                .flatMap(it -> healingMapper.modelToResultDto(it.getResults()).stream())
                .collect(Collectors.toSet());
    }

    @Override
    public void saveSuccessHealing(RecordDto.ReportRecord dto) {
        Optional<HealingResult> healingResultOptional = resultRepository.findById(dto.getHealingResultId());
        if (healingResultOptional.isPresent()) {
            HealingResult healingResult = healingResultOptional.get();
            healingResult.setSuccessHealing(dto.isSuccessHealing());
            resultRepository.save(healingResult);
            if (!dto.isSuccessHealing()) {
                amazonS3Service.moveObject(SUCCESSFUL_HEALING_BUCKET, UNSUCCESSFUL_HEALING_BUCKET, healingResult);
            } else {
                amazonS3Service.moveObject(UNSUCCESSFUL_HEALING_BUCKET, SUCCESSFUL_HEALING_BUCKET, healingResult);
            }
        }
    }

    private Healing getHealing(HealingRequestDto dto) {
        // build selector key
        String selectorId = Utils.buildKey(dto.getClassName(), dto.getMethodName(), dto.getLocator());
        // build healing key
        String healingId = Utils.buildHealingKey(selectorId, dto.getPageContent());
        return healingRepository.findById(healingId).orElseGet(() -> {
            // if no healing present
            Optional<Selector> optionalSelector = selectorRepository.findById(selectorId);
            return optionalSelector.map(element -> healingRepository.save(new Healing(healingId, element, dto.getPageContent())))
                    .orElseThrow(MissingSelectorException::new);
        });
    }

    private List<HealingResult> buildHealingResults(List<HealingResultDto> dtos, Healing healing) {
        List<HealingResult> results = dtos.stream().map(healingMapper::resultDtoToModel).peek(it -> it.setHealing(healing)).collect(Collectors.toList());
        return resultRepository.saveAll(results);
    }

    /**
     * Persist healing results
     *
     * @param healing
     * @param healingResults
     */
    private void saveHealingResults(Collection<HealingResult> healingResults, Healing healing) {
        if (!CollectionUtils.isEmpty(healing.getResults())) {
            // remove old results for given healing object
            resultRepository.deleteAll(healing.getResults());
        }

        // save new results
        List<HealingResult> results = resultRepository.saveAll(healingResults);
    }

    /**
     * Create record in report about healing
     *
     * @param result
     * @param healing
     * @param sessionId
     */
    private void createReportRecord(HealingResult result, Healing healing, String sessionId, MultipartFile screenshot) {
        if (!StringUtils.isEmpty(sessionId)) {
            String screenshotDir = "/screenshots/" + sessionId;
            String screenshotPath = persistScreenshot(screenshot, screenshotDir);
            // if healing performs during test phase, add report record
            reportRepository.findById(sessionId).ifPresent(r -> {
                r.getRecordWrapper().addRecord(healing, result, screenshotPath);
                reportRepository.save(r);
            });
        }
    }

    /**
     * @param file
     * @param filePath
     */
    private String persistScreenshot(MultipartFile file, String filePath) {
        String rootDir = Paths.get("").toAbsolutePath().toString();
        String baseDir = Paths.get(rootDir, filePath).toString();
        try {
            FileHandler.createDir(new File(baseDir));
            file.transferTo(Paths.get(baseDir, file.getOriginalFilename()));
        } catch (Exception ex) {
            log.warn("Failed to save screenshot {} in {}", file.getOriginalFilename(), baseDir);
        }
        return Paths.get(filePath, file.getOriginalFilename()).toString();
    }

    private void pushMetrics(String metrics, Map<String, String> headers, HealingResult selectedResult) {
        try {
            Summary healLatency = prometheusService.createSummaryLatency();
            healLatency.observe(Double.parseDouble(StringUtils.defaultIfEmpty(headers.get(HEALING_TIME), "0.0")));
            prometheusService.pushAndClear(headers);

            amazonS3Service.uploadObject(metrics, selectedResult,
                    StringUtils.defaultIfEmpty(headers.get(HOST_PROJECT), EMPTY_PROJECT));
        } catch (Exception ex) {
            log.warn("Error during push Prometheus metrics: {}", ex.getMessage());
        }
    }
}
