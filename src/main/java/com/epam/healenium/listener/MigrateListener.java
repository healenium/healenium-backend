package com.epam.healenium.listener;


import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.SelectorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class MigrateListener {

    private final SelectorRepository selectorRepository;
    private final SelectorService selectorService;

    @EventListener
    public void migrateSelectors(ContextRefreshedEvent event) {
        List<Selector> sourceSelectors = selectorRepository.findByCommandNull();
        selectorService.migrateSelectors(sourceSelectors);
    }

}
