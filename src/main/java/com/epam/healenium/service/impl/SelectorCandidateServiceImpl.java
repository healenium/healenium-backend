package com.epam.healenium.service.impl;

import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.elementcreators.SelectorComponent;
import com.epam.healenium.model.dto.ElementCandidate;
import com.epam.healenium.model.dto.HealingCandidateMetaData;
import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorCandidate;
import com.epam.healenium.service.SelectorCandidateService;
import com.epam.healenium.tenant.TenantTransactional;
import com.epam.healenium.treecomparing.HeuristicNodeDistance;
import com.epam.healenium.treecomparing.JsoupHTMLParser;
import com.epam.healenium.treecomparing.LCSPathDistance;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Path;
import com.epam.healenium.treecomparing.PathFinder;
import com.epam.healenium.treecomparing.Scored;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@TenantTransactional
public class SelectorCandidateServiceImpl implements SelectorCandidateService {
    private final DynamicSettings dynamicSettings;

    public static final String PLAYWRIGHT_LOCATOR_TYPE = "locator";

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
    public List<SelectorCandidate> getCandidates(RequestDto dto, ReferenceElementsDto ref) {
        Node destination = parseTree(dto.getPageContent());

        return ref.getPaths().stream()
                .map(path -> findSelectorCandidate(path, destination, ref, dto.getLocatorType()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Node parseTree(String tree) {
        return new JsoupHTMLParser().parse(new ByteArrayInputStream(tree.getBytes(StandardCharsets.UTF_8)));
    }

    private SelectorCandidate findSelectorCandidate(List<Node> path, Node destination, ReferenceElementsDto ref,
                                                    String locatorType) {
        PathFinder pathFinder = new PathFinder(new LCSPathDistance(), new HeuristicNodeDistance());
        AbstractMap.SimpleImmutableEntry<Integer, Map<Double,
                List<AbstractMap.SimpleImmutableEntry<Node, Integer>>>> scoresToNodes =
                pathFinder.findScoresToNodes(new Path(path.toArray(new Node[0])), destination);
        List<Scored<Node>> scores = pathFinder.getSortedNodes(scoresToNodes.getValue(), 1000, dynamicSettings.getScoreCap());
        log.debug("List<Scored<Node>> scores size: {}", scores.size());

        List<HealingCandidateMetaData> candidateMetaData = getAllHealingCandidateMetaData(scoresToNodes);

        List<ElementCandidate> candidates = scores.stream()
                .map(scored -> toElementCandidate(scored, ref, locatorType))
                .filter(Objects::nonNull)
                .limit(dynamicSettings.getRecoveryTries())
                .collect(Collectors.toCollection(ArrayList::new));
        return new SelectorCandidate()
                .setElementCandidates(candidates)
                .setPath(path)
                .setScores(scores)
                .setCandidateMetaData(candidateMetaData);
    }

    /**
     * @param curPathHeightToScores - all PathToNode candidate collection
     * @return list healingCandidateDto for metrics
     */
    private List<HealingCandidateMetaData> getAllHealingCandidateMetaData(AbstractMap.SimpleImmutableEntry<Integer, Map<Double,
            List<AbstractMap.SimpleImmutableEntry<Node, Integer>>>> curPathHeightToScores) {
        Integer curPathHeight = curPathHeightToScores.getKey();
        Map<Double, List<AbstractMap.SimpleImmutableEntry<Node, Integer>>> scoresToNodes = curPathHeightToScores.getValue();
        return scoresToNodes.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .flatMap(score -> scoresToNodes.get(score).stream()
                        .map(it -> new HealingCandidateMetaData(score, it.getValue(), curPathHeight, it.getKey())))
                .limit(10)
                .toList();
    }

    protected ElementCandidate toElementCandidate(Scored<Node> node, ReferenceElementsDto ref, String locatorType) {
        List<com.epam.healenium.model.Locator> locators = new ArrayList<>();
        if (dynamicSettings.getSelectorType().equals("xpath")) {
            // todo get candidate by xpath from AI service
        }
        for (Set<SelectorComponent> detailLevel : selectorDetailLevels) {
            locators.add(construct(node.getValue(), detailLevel, locatorType));
        }
        log.debug("[Get Selector Candidates] raw candidates before filter: {}", locators);
        @SuppressWarnings("java:S6204")
        List<com.epam.healenium.model.Locator> filtered = locators.stream()
                .filter(locator -> !ref.getUnsuccessfulLocators().contains(locator))
                .collect(Collectors.toList());

        log.debug("[Get Selector Candidates] raw candidates after filter: {}", filtered);
        return new ElementCandidate().setNodeScored(node).setLocators(filtered);
    }

    protected com.epam.healenium.model.Locator construct(Node node, Set<SelectorComponent> detailLevel, String locatorType) {
        StringBuilder selectorBuilder = new StringBuilder();

        for (SelectorComponent component : detailLevel) {
            selectorBuilder.append(component.createComponent(node));
        }
        if (PLAYWRIGHT_LOCATOR_TYPE.equals(locatorType)) {
            return new com.epam.healenium.model.Locator(selectorBuilder.toString(), PLAYWRIGHT_LOCATOR_TYPE);
        } else {
            return new com.epam.healenium.model.Locator(selectorBuilder.toString(), "By.cssSelector");
        }

    }

}
