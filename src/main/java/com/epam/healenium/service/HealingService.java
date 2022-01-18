package com.epam.healenium.service;

import com.epam.healenium.model.dto.HealingDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.HealingResultRequestDto;
import com.epam.healenium.model.dto.LastHealingDataDto;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;

import java.util.Map;
import java.util.Set;

public interface HealingService {

    /**
     * Healing attempt of given target selector in presented page source
     *
     * @param dto
     * @param headers
     */
    void saveHealing(HealingResultRequestDto dto, Map<String, String> headers);

    /**
     * Store selector
     *
     * @param request
     */
    void saveSelector(SelectorRequestDto request);

    /**
     * @param dto
     * @return
     */
    LastHealingDataDto getSelectorPath(RequestDto dto);

    /**
     * @param dto
     * @return
     */
    Set<HealingDto> getHealings(RequestDto dto);

    /**
     * Search for stored healing results
     *
     * @param dto
     * @return
     */
    Set<HealingResultDto> getHealingResults(RequestDto dto);

    /**
     * Store successHealing
     *
     * @param dto
     */
    void saveSuccessHealing(RecordDto.ReportRecord dto);
}
