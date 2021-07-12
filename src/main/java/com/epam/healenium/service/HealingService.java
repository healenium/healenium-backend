package com.epam.healenium.service;

import com.epam.healenium.model.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

public interface HealingService {

    /**
     * Healing attempt of given target selector in presented page source
     * @param dto
     * @param screenshot
     * @param headers
     * @param metrics
     */
    void saveHealing(HealingRequestDto dto, MultipartFile screenshot, Map<String, String> headers, String metrics);

    /**
     * Store selector
     * @param request
     */
    void saveSelector(SelectorRequestDto request);

    /**
     *
     * @param dto
     * @return
     */
    LastHealingDataDto getSelectorPath(RequestDto dto);

    /**
     *
     * @param dto
     * @return
     */
    Set<HealingDto> getHealings(RequestDto dto);

    /**
     * Search for stored healing results
     * @param dto
     * @return
     */
    Set<HealingResultDto> getHealingResults(RequestDto dto);

    /**
     * Store successHealing
     * @param dto
     */
    void saveSuccessHealing(RecordDto.ReportRecord dto);
}
