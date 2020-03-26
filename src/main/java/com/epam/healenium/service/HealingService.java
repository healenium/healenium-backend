package com.epam.healenium.service;

import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface HealingService {

    /**
     * Healing attempt of given target selector in presented page source
     * @param dto
     * @param sessionId
     * @return
     */
    void saveHealing(HealingRequestDto dto, MultipartFile[] screenshots, String sessionId);

    /**
     * Store selector
     * @param request
     */
    void saveSelector(SelectorRequestDto request);

    /**
     * Search for stored healing results
     * @param dto
     * @return
     */
    Set<HealingResultDto> getHealingResults(RequestDto dto);

}
