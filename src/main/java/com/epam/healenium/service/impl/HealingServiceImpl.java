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
import com.epam.healenium.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.io.FileHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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
    public void saveHealing(HealingRequestDto dto, MultipartFile[] screenshots, String sessionId) {
        // collect healing results
        Collection<HealingResult> healingResults = buildHealingResults(dto.getResults(), screenshots, sessionId);
        HealingResult selectedResult = healingResults.stream()
                .filter(it-> {
                    String firstLocator, secondLocator;
                    firstLocator = it.getLocator().getValue();
                    secondLocator = dto.getUsedResult().getLocator().getValue();
                    return firstLocator.equals(secondLocator);
                })
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("Internal exception! Somehow we lost selected healing result on save"));
        // obtain healing
        Healing healing = saveHealingResults(healingResults, getHealing(dto)) ;
        // add report record
        createReportRecord( selectedResult, healing, sessionId);
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

    private List<HealingResult> buildHealingResults(List<HealingResultDto> dtos, MultipartFile[] screenshots, String sessionId) {
        String screenshotDir = "/screenshots/" + sessionId;
        Map<String, MultipartFile> screenshotMap = Arrays.stream(screenshots)
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity()));
        return dtos.stream().map(it -> {
            persistScreenshot(screenshotMap.get(it.getScreenshotName()), screenshotDir);
            HealingResult result = healingMapper.resultDtoToModel(it);
            result.setScreenshotPath(screenshotDir + "/" + it.getScreenshotName());
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * Persist healing results
     *
     * @param healing
     * @param healingResults
     * @return
     */
    private Healing saveHealingResults(Collection<HealingResult> healingResults, Healing healing) {
        if (!CollectionUtils.isEmpty(healing.getResults())) {
            // remove old results for given healing object
            resultRepository.deleteAll(healing.getResults());
        }
        // save new results
        List<HealingResult> results = resultRepository.saveAll(healingResults);
        // link new results with healing document
        healing.setResults(new HashSet<>(results));
        return healingRepository.save(healing);
    }

    /**
     * Create record in report about healing
     * @param result
     * @param healing
     * @param sessionId
     */
    private void createReportRecord(HealingResult result, Healing healing, String sessionId){
        if (!StringUtils.isEmpty(sessionId)) {
            // if healing performs during test phase, add report record
            reportRepository.findById(sessionId).ifPresent(r -> {
                    r.addRecord(healing, result);
                    reportRepository.save(r);
            });
        }
    }

    /**
     *
     * @param file
     * @param filePath
     */
    private void persistScreenshot(MultipartFile file, String filePath){
        String rootDir = Paths.get("").toAbsolutePath().toString();
        String baseDir = Paths.get(rootDir, filePath).toString();
        try{
            FileHandler.createDir(new File(baseDir));
            file.transferTo(Paths.get(baseDir, file.getOriginalFilename()));
        } catch (Exception ex){
            log.warn("Failed to save screenshot {} in {}", file.getOriginalFilename(), baseDir);
        }
    }
}
