package com.epam.healenium.controller;

import com.epam.healenium.model.dto.HealingDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.HealingResultRequestDto;
import com.epam.healenium.model.dto.LastHealingDataDto;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.service.HealingService;
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

@Slf4j()
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
        log.info("Save locator: {}({})", request.getType(), request.getLocator());
        healingService.saveSelector(request);
    }

    /**
     * Getting last valid path for provided request
     *
     * @param dto
     * @return
     */
    @GetMapping()
    public LastHealingDataDto getPath(RequestDto dto) {
        return healingService.getSelectorPath(dto);
    }

    /**
     * Saving heal result for specific selector
     *
     * @param dto
     * @param headers
     * @return
     */
    @PostMapping("/healing")
    public void save(@Valid @RequestBody List<HealingResultRequestDto> dto,
                     @RequestHeader Map<String, String> headers) {
        if (StringUtils.isEmpty(headers.get(SESSION_KEY_V1)) && StringUtils.isEmpty(headers.get(SESSION_KEY_V2))) {
            log.warn("Session key is not present. Current issue would not be presented in any reports, but still available in replacement!");
        }
        dto.forEach(requestDto -> healingService.saveHealing(requestDto, headers));
    }

    /**
     * Getting healing with their results for provided request
     *
     * @param dto
     * @return
     */
    @GetMapping("/healing")
    public Set<HealingDto> getHealings(RequestDto dto) {
        return healingService.getHealings(dto);
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

    @PostMapping("/healing/success")
    public void successHealing(@Valid @RequestBody RecordDto.ReportRecord dto) {
        healingService.saveSuccessHealing(dto);
    }

    @GetMapping("/selectors")
    public ModelAndView get() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("selector");
        modelAndView.addObject("dto", healingService.getAllSelectors());
        return modelAndView;
    }
}