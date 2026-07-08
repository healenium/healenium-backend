package com.epam.healenium.service;

import com.epam.healenium.model.dto.*;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HealingService {

    /**
     * Healing attempt of given target selector in presented page source
     *
     * @param dto
     * @param headers
     */
    void saveHealing(HealingRequestDto dto, Map<String, String> headers);

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

    boolean validateReference(ReferenceElementsDto referenceElements);

    List<By> getCandidates(RequestDto dto, ReferenceElementsDto referenceElements);

}
