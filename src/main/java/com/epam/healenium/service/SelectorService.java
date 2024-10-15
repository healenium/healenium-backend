package com.epam.healenium.service;

import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.ConfigSelectorDto;
import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorDto;
import com.epam.healenium.model.dto.SelectorRequestDto;

import java.util.List;

public interface SelectorService {
    void saveSelector(SelectorRequestDto request);

    ReferenceElementsDto getReferenceElements(RequestDto dto);

    List<SelectorRequestDto> getAllSelectors();

    ConfigSelectorDto getConfigSelectors();

    void setSelectorStatus(SelectorDto dto);

    String getSelectorId(String locator, String url, String command, boolean urlForKey);

    void migrate();

    void migrateSelectors(List<Selector> sourceSelectors);
}
