package com.epam.healenium.service.impl;


import com.epam.healenium.mapper.SelectorMapper;
import com.epam.healenium.model.SessionContext;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.ConfigSelectorDto;
import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.node.NodeService;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class SelectorServiceImpl implements SelectorService {

    @Value("${app.selector.key.url-for-key}")
    private boolean urlForKey;

    @Value("${app.healing.elements}")
    private boolean findElementsAutoHealing;

    private PassiveExpiringMap<String, SessionContext> sessionContextCache = new PassiveExpiringMap<>(8, TimeUnit.HOURS);

    private final SelectorRepository selectorRepository;
    private final SelectorMapper selectorMapper;
    private final HealingRepository healingRepository;

    @Override
    public void saveSelector(SelectorRequestDto request) {
        if (CollectionUtils.isEmpty(request.getNodePath())) {
            log.debug("[Save Elements] Parse Node Path");
            parseNodePath(request);
        }
        String id = getSelectorId(request.getLocator(), request.getUrl(), request.getCommand(), urlForKey);
        Optional<Selector> existSelector = selectorRepository.findById(id);
        final Selector selector = selectorMapper.toSelector(request, id, existSelector, findElementsAutoHealing);
        selectorRepository.save(selector);
        log.debug("[Save Elements] Selector: {}", selector);
    }

    @Override
    public ReferenceElementsDto getReferenceElements(RequestDto dto) {
        String selectorId = getSelectorId(dto.getLocator(), dto.getUrl(), dto.getCommand(), urlForKey);
        List<List<Node>> paths = selectorRepository.findById(selectorId)
                .map(t -> t.getNodePathWrapper().getNodePath())
                .orElse(Collections.emptyList());
        return new ReferenceElementsDto()
                .setPaths(paths);
    }

    @Override
    public List<RequestDto> getAllSelectors() {
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


    private void parseNodePath(SelectorRequestDto request) {
        try {
            SessionContext sessionContext = sessionContextCache.get(request.getSessionId());
            if (sessionContext == null || sessionContext.getRemoteWebDriver() == null) {
                log.warn("[Save Elements] SessionContext or RemoteWebDriver not found from cache.");
                return;
            }
            NodeService nodeService = sessionContext.getNodeService();
            RemoteWebDriver remoteWebDriver = sessionContext.getRemoteWebDriver();
            String url = nodeService.getCurrentUrl(remoteWebDriver);
            log.debug("[Save Elements] Parse Node Path to URL: {}", url);
            request.setUrl(url);
            List<String> ids = request.getElementIds();
            List<List<Node>> nodes = ids.stream()
                    .map(id -> {
                        RemoteWebElement remoteWebElement = new RemoteWebElement();
                        remoteWebElement.setId(id);
                        remoteWebElement.setParent(remoteWebDriver);
                        return nodeService.getNodePath(remoteWebDriver, remoteWebElement);
                    })
                    .collect(Collectors.toList());
            request.setNodePath(nodes);
            log.debug("[Save Elements] Parse Node size: {}", nodes.size());
        } catch (Exception e) {
            log.error("[Save Elements] Error during parseNodePath. Message: {} Ex: {}", e.getMessage(), e);
        }
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

}
