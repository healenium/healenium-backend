package com.epam.healenium.service.impl;

import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.exception.MissingSelectorException;
import com.epam.healenium.mapper.HealingMapper;
import com.epam.healenium.elementcreators.SelectorComponent;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.*;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.rest.AmazonRestService;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.service.ReportService;
import com.epam.healenium.service.selector.SelectorIdStrategy;
import com.epam.healenium.specification.HealingSpecBuilder;
import com.epam.healenium.treecomparing.*;
import com.epam.healenium.util.StreamUtils;
import com.epam.healenium.util.Utils;
import com.epam.healenium.tenant.TenantTransactional;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.healenium.constants.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@TenantTransactional
public class HealingServiceImpl implements HealingService {

    private final DynamicSettings dynamicSettings;
    private final HealingRepository healingRepository;
    private final SelectorRepository selectorRepository;
    private final SelectorIdStrategy selectorIdStrategy;
    private final HealingResultRepository resultRepository;
    private final ReportService reportService;
    private final HealingMapper healingMapper;
    private final AmazonRestService amazonRestService;

    private final List<Set<SelectorComponent>> selectorDetailLevels = Collections.unmodifiableList(TEMP);

    private final static List<Set<SelectorComponent>> TEMP = new ArrayList<Set<SelectorComponent>>() {{
        add(EnumSet.of(SelectorComponent.TAG, SelectorComponent.ID));
        add(EnumSet.of(SelectorComponent.TAG, SelectorComponent.CLASS));
        add(EnumSet.of(SelectorComponent.PARENT, SelectorComponent.TAG, SelectorComponent.ID, SelectorComponent.CLASS));
        add(EnumSet.of(SelectorComponent.PARENT, SelectorComponent.TAG, SelectorComponent.CLASS, SelectorComponent.POSITION));
        add(EnumSet.of(SelectorComponent.PARENT, SelectorComponent.TAG, SelectorComponent.ID, SelectorComponent.CLASS, SelectorComponent.ATTRIBUTES));
        add(EnumSet.of(SelectorComponent.PATH));
    }};

    @Override
    public void saveHealing(HealingRequestDto dto, Map<String, String> headers) {
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
                .orElseThrow(() -> new IllegalArgumentException("[Save Healing] Internal exception! Somehow we lost selected healing result on save"));
        // add report record
        String sessionKey = Utils.getSessionKey(headers);
        reportService.createReportRecord(selectedResult, healing, sessionKey, dto.getScreenshot());
        if (dynamicSettings.isCollectMetrics()) {
            pushMetrics(dto.getMetrics(), headers, selectedResult, dto.getUrl());
        }
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
        String selectorId = selectorIdStrategy.getSelectorId(dto.getLocator(), dto.getUrl(), dto.getCommand(), dynamicSettings.isKeySelectorUrl());
        log.debug("[Get Healing Result] Selector ID: {}", selectorId);
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
            if (dynamicSettings.isCollectMetrics()) {
                moveMetrics(dto, healingResult);
            }
        }
    }

    private Healing getHealing(HealingRequestDto dto) {
        // build selector key
        String selectorId = selectorIdStrategy.getSelectorId(dto.getLocator(), dto.getUrl(), dto.getCommand(), dynamicSettings.isKeySelectorUrl());
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
     */
    private void saveHealingResults(Collection<HealingResult> healingResults, Healing healing) {
        if (!CollectionUtils.isEmpty(healing.getResults())) {
            // remove old results for given healing object
            resultRepository.deleteAll(healing.getResults());
        }

        // save new results
        List<HealingResult> results = resultRepository.saveAll(healingResults);
    }

    private void pushMetrics(String metrics, Map<String, String> headers, HealingResult selectedResult, String url) {
        try {
            if (metrics != null) {
                log.debug("[Save Healing] Push Metrics: {}", selectedResult);
                amazonRestService.uploadMetrics(metrics, selectedResult,
                        StringUtils.defaultIfEmpty(headers.get(HOST_PROJECT), EMPTY_PROJECT), url);
            }
        } catch (Exception ex) {
            log.warn("[Save Healing] Error during push metrics: {}", ex.getMessage());
        }
    }

    private void moveMetrics(RecordDto.ReportRecord dto, HealingResult healingResult) {
        try {
            if (!dto.isSuccessHealing()) {
                log.debug("[Set Healing Status] Set 'Unsuccessful' status");
                amazonRestService.moveMetrics(SUCCESSFUL_HEALING_BUCKET, healingResult);
            } else {
                log.debug("[Set Healing Status] Set 'Successful' status");
                amazonRestService.moveMetrics(UNSUCCESSFUL_HEALING_BUCKET, healingResult);
            }
        } catch (Exception ex) {
            log.warn("[Set Healing Status] Error during move metrics: {}", ex.getMessage());
        }
    }


    @Override
    public boolean validateReference(ReferenceElementsDto referenceElements) {
        log.debug("[Get Reference] Response: {})", referenceElements);
        if (referenceElements.getPaths().isEmpty()) {
            log.warn("New element locator have not been found. There is no reference data to selector in the database." +
                    "\nMake sure that: " +
                    "\n- There is selector on the page /selectors/ and type: single, if not then you have to run successful tests." +
                    "\n- Your locator was changed on the page and not in code.");
            return false;
        }
        return true;
    }

    @Override
    public List<By> getCandidates(RequestDto dto, ReferenceElementsDto referenceElements) {
        String targetPage = dto.getPageContent();
        Node destination = parseTree(targetPage);

        log.warn("Trying to heal...");
        for (List<Node> nodes : referenceElements.getPaths()) {
            return findNewLocations(nodes, destination, referenceElements);
        }
        return new ArrayList<>();
    }

    public Node parseTree(String tree) {
        return new JsoupHTMLParser().parse(new ByteArrayInputStream(tree.getBytes(StandardCharsets.UTF_8)));
    }

    public List<By> findNewLocations(List<Node> paths, Node destination, ReferenceElementsDto referenceElements) {
        PathFinder pathFinder = new PathFinder(new LCSPathDistance(), new HeuristicNodeDistance());
        AbstractMap.SimpleImmutableEntry<Integer, Map<Double, List<AbstractMap.SimpleImmutableEntry<Node, Integer>>>> scoresToNodes =
                pathFinder.findScoresToNodes(new Path(paths.toArray(new Node[0])), destination);
        // TODO get guessCap from settings
        List<Scored<Node>> scoreds = pathFinder.getSortedNodes(scoresToNodes.getValue(), 1000, 0.6);

        List<By> healedElements = scoreds.stream()
                .map(node -> toLocator(node, referenceElements))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return healedElements;
    }

    public By toLocator(Scored<Node> node, ReferenceElementsDto referenceElements) {
        By locator = null;
//        if (useXPath(engine)) {
//            String xpath = createXPathFromElement(node.getValue(), engine);
//            locator = By.xpath(xpath);
//            if (isUnsuccessLocator(locator, context, engine)) {
//                return null;
//            }
//        }

        for (Set<SelectorComponent> detailLevel : selectorDetailLevels) {
            locator = construct(node.getValue(), detailLevel);
        }

        if (isUnsuccessLocator(locator, referenceElements)) {
            return null;
        }
        return locator;
    }

    public By construct(Node node, Set<SelectorComponent> detailLevel) {
        return By.cssSelector(detailLevel.stream()
                .map(component -> component.createComponent(node))
                .collect(Collectors.joining()));
    }


    private boolean isUnsuccessLocator(By locator, ReferenceElementsDto referenceElements) {
        List<Locator> unsuccessfulLocators = referenceElements.getUnsuccessfulLocators();
        Locator candidate = new Locator(((By.ByCssSelector) locator).getRemoteParameters().using(),
                ((By.ByCssSelector) locator).getRemoteParameters().value().toString());
        return unsuccessfulLocators != null && unsuccessfulLocators.contains(candidate);
    }

}
