package com.epam.healenium.listener;


import com.epam.healenium.mapper.SelectorMapper;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.SelectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class MigrateListener {

    @Value("${app.selector.key.url-for-key}")
    private boolean urlForKey;
    @Value("${app.selector.key.path-for-key}")
    private boolean pathForKey;

    private final SelectorRepository selectorRepository;
    private final SelectorService selectorService;
    private final HealingRepository healingRepository;
    private final SelectorMapper selectorMapper;

    @EventListener
    public void migrateSelectors(ContextRefreshedEvent event) {
        List<Selector> sourceSelectors = selectorRepository.findByCommandNull();
        if (sourceSelectors.isEmpty()) {
            log.debug("[Migrate] There is no selectors to migrate");
            return;
        }
        Map<String, Selector> sourceToTarget = new HashMap<>();
        migrateSelectors(sourceSelectors, sourceToTarget);
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

    private void migrateSelectors(List<Selector> sourceSelectors, Map<String, Selector> sourceToTarget) {
        for (Selector sourceSelector : sourceSelectors) {
            Selector targetSelector = selectorMapper.cloneSelector(sourceSelector)
                    .setUid(selectorService.getSelectorId(sourceSelector.getLocator().getValue(), sourceSelector.getUrl(),
                            sourceSelector.getCommand(), urlForKey, pathForKey));
            if (sourceSelector.getNodePathWrapper().getNodePath().size() > 1) {
                targetSelector.setCommand("findElements")
                        .setEnableHealing(false);
            } else {
                targetSelector.setCommand("findElement")
                        .setEnableHealing(true);
            }
            sourceToTarget.put(sourceSelector.getUid(), targetSelector);
        }
    }

}
