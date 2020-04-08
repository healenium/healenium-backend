package com.epam.healenium.service.impl;

import com.epam.healenium.exception.MissingSelectorException;
import com.epam.healenium.mapper.HealingMapper;
import com.epam.healenium.mapper.SelectorMapper;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.io.FileHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public void saveSelector(SelectorRequestDto request) {
        final Selector selector = selectorMapper.dtoToDocument(request);
        selectorRepository.save(selector);
    }

    @Override
    public List<Node> getSelectorPath(RequestDto dto) {
        String selectorId = Utils.buildKey(dto.getClassName(), dto.getMethodName(), dto.getLocator());
        return selectorRepository.findById(selectorId).map(Selector::getNodePath).orElse(Collections.emptyList());
    }

    @Override
    public void saveHealing(HealingRequestDto dto, MultipartFile screenshot, String sessionId) {
        // obtain healing
        Healing healing = getHealing(dto);
        // collect healing results
        Collection<HealingResult> healingResults = buildHealingResults(dto.getResults(), healing);
        HealingResult selectedResult = healingResults.stream()
                .filter(it-> {
                    String firstLocator, secondLocator;
                    firstLocator = it.getLocator().getValue();
                    secondLocator = dto.getUsedResult().getLocator().getValue();
                    return firstLocator.equals(secondLocator);
                })
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("Internal exception! Somehow we lost selected healing result on save"));
        // add report record
        createReportRecord( selectedResult, healing, sessionId, screenshot);
    }


    @Override
    public Set<HealingResultDto> getHealingResults(RequestDto dto) {
        String selectorId = Utils.buildKey(dto.getClassName(), dto.getMethodName(), dto.getLocator());
        return healingRepository.findBySelectorId(selectorId).stream()
                .flatMap(it -> healingMapper.modelToResultDto(it.getResults()).stream())
                .collect(Collectors.toSet());
    }

    private Healing getHealing(HealingRequestDto dto){
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
        List<HealingResult> results = dtos.stream().map(healingMapper::resultDtoToModel).peek(it-> it.setHealing(healing)).collect(Collectors.toList());
        return resultRepository.saveAll(results);
    }

    /**
     * Persist healing results
     *
     * @param healing
     * @param healingResults
     * @return
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
     * @param result
     * @param healing
     * @param sessionId
     */
    private void createReportRecord(HealingResult result, Healing healing, String sessionId, MultipartFile screenshot){
        if (!StringUtils.isEmpty(sessionId)) {
            String screenshotDir = "/screenshots/" + sessionId;
            String screenshotPath = persistScreenshot(screenshot, screenshotDir);
            // if healing performs during test phase, add report record
            reportRepository.findById(sessionId).ifPresent(r -> {
                    r.addRecord(healing, result, screenshotPath);
                    reportRepository.save(r);
            });
        }
    }

    /**
     *
     * @param file
     * @param filePath
     */
    private String persistScreenshot(MultipartFile file, String filePath){
        String rootDir = Paths.get("").toAbsolutePath().toString();
        String baseDir = Paths.get(rootDir, filePath).toString();
        try{
            FileHandler.createDir(new File(baseDir));
            file.transferTo(Paths.get(baseDir, file.getOriginalFilename()));
        } catch (Exception ex){
            log.warn("Failed to save screenshot {} in {}", file.getOriginalFilename(), baseDir);
        }
        return Paths.get(filePath,file.getOriginalFilename()).toString();
    }
}
