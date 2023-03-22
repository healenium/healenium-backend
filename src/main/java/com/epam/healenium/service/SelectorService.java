package com.epam.healenium.service;

import com.epam.healenium.model.dto.ConfigSelectorDto;
import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.dto.SessionDto;

import java.util.List;

public interface SelectorService {
    void saveSelector(SelectorRequestDto request);

    void restoreSession(SessionDto sessionDto);

    ReferenceElementsDto getReferenceElements(RequestDto dto);

    List<RequestDto> getAllSelectors();

    ConfigSelectorDto getConfigSelectors();

    void setSelectorStatus(SelectorDto dto);

    String getSelectorId(String locator, String url, String command, boolean urlForKey, boolean pathForKey);
}
