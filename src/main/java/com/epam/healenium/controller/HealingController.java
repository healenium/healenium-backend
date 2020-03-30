package com.epam.healenium.controller;

import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.treecomparing.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/healenium")
@RequiredArgsConstructor
public class HealingController {

    private final HealingService healingService;

    /**
     * Saving information about a successfully found item
     *
     * @param request
     */
    @PostMapping()
    public void save(@Valid @RequestBody SelectorRequestDto request) {
        healingService.saveSelector(request);
    }

    /**
     *
     * @param dto
     * @return
     */
    @GetMapping()
    public List<Node> getPath(RequestDto dto) {
        return healingService.getSelectorPath(dto);
    }

    /**
     * Saving heal result for specific selector
     * @param dto
     * @param sessionId
     * @return
     */
    @PostMapping("/healing")
    public void save(@Valid @RequestParam("dto") HealingRequestDto dto, @RequestParam("screenshot") MultipartFile screenshot, @RequestHeader(value = "sessionKey", required = false) String sessionId) {
        if (StringUtils.isEmpty(sessionId)) {
            log.warn("Session key is not present. Current issue would not be presented in any reports, but still available in replacement!");
        }
        healingService.saveHealing(dto, screenshot, sessionId);
    }

    /**
     * Getting healing results for selector
     *
     * @param dto
     * @return
     */
    @GetMapping("/healing/results")
    public Set<HealingResultDto> getResults(RequestDto dto) {
        return healingService.getHealingResults(dto);
    }

}