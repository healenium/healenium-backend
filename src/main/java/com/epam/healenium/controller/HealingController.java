package com.epam.healenium.controller;

import com.epam.healenium.model.dto.ConfigSelectorDto;
import com.epam.healenium.model.dto.HealingDto;
import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.dto.SessionDto;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.service.SelectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.healenium.constants.Constants.SESSION_KEY_V1;
import static com.epam.healenium.constants.Constants.SESSION_KEY_V2;

@Slf4j(topic = "healenium")
@RestController
@RequestMapping("/healenium")
@RequiredArgsConstructor
public class HealingController {

    private final HealingService healingService;
    private final SelectorService selectorService;

    /**
     * Saving information about a successfully found item
     *
     * @param request
     */
    @PostMapping()
    public void save(@Valid @RequestBody SelectorRequestDto request) {
        log.info("[Save Elements] Request: {}({})", request.getType(), request.getLocator());
        selectorService.saveSelector(request);
    }

    /**
     * Getting last valid path for provided request
     *
     * @param dto
     * @return
     */
    @GetMapping()
    public ReferenceElementsDto getReferenceElements(RequestDto dto) {
        log.info("[Get Reference] Request: {})", dto);
        ReferenceElementsDto referenceElements = selectorService.getReferenceElements(dto);
        log.debug("[Get Reference] Response: {})", referenceElements);
        return referenceElements;
    }

    /**
     * Getting all saved selectors and config
     *
     * @return
     */
    @GetMapping("/elements")
    public ConfigSelectorDto getElements() {
        ConfigSelectorDto configSelectors = selectorService.getConfigSelectors();
        log.debug("[Get Elements] Response: {}", configSelectors);
        return configSelectors;
    }

    /**
     * Saving heal result for specific selector
     *
     * @param dto
     * @param headers
     * @return
     */
    @PostMapping("/healing")
    public void save(@Valid @RequestBody List<HealingRequestDto> dto,
                     @RequestHeader Map<String, String> headers) {
        log.debug("[Save Healing] Request: {}. Headers: {}", dto, headers);
        if (StringUtils.isEmpty(headers.get(SESSION_KEY_V1)) && StringUtils.isEmpty(headers.get(SESSION_KEY_V2))) {
            log.warn("Session key is not present. Current issue would not be presented in any reports, but still available in replacement!");
        }
        dto.forEach(requestDto -> healingService.saveHealing(requestDto, headers));
    }

    /**
     * Restore session to parse dom for proxy type
     *
     * @param dto
     * @return
     */
    @PostMapping("/session")
    public void session(@Valid @RequestBody SessionDto dto) {
        log.debug("[Restore Session] Request: {}", dto);
    }

    /**
     * Getting healing with their results for provided request
     *
     * @param dto
     * @return
     */
    @GetMapping("/healing")
    public Set<HealingDto> getHealings(RequestDto dto) {
        log.debug("[Get Healing] Request: {}", dto);
        Set<HealingDto> healings = healingService.getHealings(dto);
        log.debug("[Get Healing] Response: {}", healings);
        return healings;
    }

    /**
     * Getting healing results for selector
     *
     * @param dto
     * @return
     */
    @GetMapping("/healing/results")
    public Set<HealingResultDto> getResults(RequestDto dto) {
        log.debug("[Get Healing Result] Request: {}", dto);
        Set<HealingResultDto> healingResults = healingService.getHealingResults(dto);
        log.debug("[Get Healing Result] Response: {}", healingResults);
        return healingResults;
    }

    /**
     * Setting status of healing
     *
     * @param dto
     * @return
     */
    @PostMapping("/healing/success")
    public void successHealing(@Valid @RequestBody RecordDto.ReportRecord dto) {
        log.debug("[Set Healing Status] Request: {}", dto);
        healingService.saveSuccessHealing(dto);
    }

    /**
     * Getting all selectors for selector.html
     *
     * @return
     */
    @GetMapping("/selectors")
    public ModelAndView get() {
        log.debug("[Get Selector Page]");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("selector");
        modelAndView.addObject("dto", selectorService.getAllSelectors());
        return modelAndView;
    }

    /**
     * Setting status (enable/disable) to healing
     *
     * @param dto
     * @return
     */
    @PostMapping("/selector/status")
    public void setSelectorStatus(@Valid @RequestBody SelectorDto dto) {
        log.debug("[Set Selector Status] Request: {}", dto);
        selectorService.setSelectorStatus(dto);
    }

    @GetMapping("/migrate")
    public ModelAndView migrate() {
        log.debug("[Migrate Selectors]");
        selectorService.migrate();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("message", "The migration of selectors was successful.");
        return modelAndView;
    }

}