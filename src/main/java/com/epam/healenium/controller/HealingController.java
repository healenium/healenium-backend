package com.epam.healenium.controller;

import com.epam.healenium.model.dto.Locator;
import com.epam.healenium.model.dto.ConfigSelectorDto;
import com.epam.healenium.model.dto.HealingDto;
import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorCandidate;
import com.epam.healenium.model.dto.SelectorDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.dto.SessionDto;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.service.SelectorCandidateService;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.util.Utils;
import com.epam.healenium.tenant.TenantTxFacade;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/healenium")
public class HealingController {

    private final TenantTxFacade tenantTx;
    private final HealingService healingService;
    private final SelectorService selectorService;
    private final SelectorCandidateService selectorCandidateService;

    public HealingController(TenantTxFacade tenantTx, HealingService healingService, SelectorService selectorService, SelectorCandidateService selectorCandidateService) {
        this.tenantTx = tenantTx;
        this.healingService = healingService;
        this.selectorService = selectorService;
        this.selectorCandidateService = selectorCandidateService;
    }

    /**
     * Saving information about a successfully found item
     */
    @PostMapping()
    public void save(@Valid @RequestBody SelectorRequestDto request) {
        tenantTx.required(() -> {
            log.info("[Save Elements] Request: {}({})", request.getType(), request.getLocator());
            selectorService.saveSelector(request);
        });
    }

    /**
     * Getting last valid path for provided request
     */
    @GetMapping()
    public ReferenceElementsDto getReferenceElements(RequestDto dto) {
        return tenantTx.required(() -> {
            log.debug("[Get Reference] Request: {})", dto);
            ReferenceElementsDto referenceElements = selectorService.getReferenceElements(dto);
            log.debug("[Get Reference] Response: {})", referenceElements);
            return referenceElements;
        });
    }

    /**
     * Getting all saved selectors and config
     */
    @GetMapping("/elements")
    public ConfigSelectorDto getElements() {
        return tenantTx.required(() -> {
            ConfigSelectorDto configSelectors = selectorService.getConfigSelectors();
            log.debug("[Get Elements] Response: {}", configSelectors);
            return configSelectors;
        });
    }

    /**
     * Saving heal result for specific selector
     */
    @PostMapping("/healing")
    public void save(@Valid @RequestBody List<HealingRequestDto> dto,
                     @RequestHeader Map<String, String> headers) {
        tenantTx.required(() -> {
            log.debug("[Save Healing] Request: {}. Headers: {}", dto, headers);
            String sessionKey = Utils.getSessionKey(headers);
            if (!StringUtils.hasText(sessionKey)) {
                log.warn("Session key is not present. Current issue would not be presented in any reports, but still available in replacement!");
            }
            dto.forEach(requestDto -> healingService.saveHealing(requestDto, headers));
        });
    }

    /**
     * Restore session to parse dom for proxy type
     */
    @PostMapping("/session")
    public void session(@Valid @RequestBody SessionDto dto) {
        log.debug("[Restore Session] Request: {}", dto);
    }

    /**
     * Getting healing with their results for provided request
     */
    @GetMapping("/healing")
    public Set<HealingDto> getHealings(RequestDto dto) {
        return tenantTx.required(() -> {
            log.debug("[Get Healing] Request: {}", dto);
            Set<HealingDto> healings = healingService.getHealings(dto);
            log.debug("[Get Healing] Response: {}", healings);
            return healings;
        });
    }

    /**
     * Getting healing results for selector
     */
    @GetMapping("/healing/results")
    public Set<HealingResultDto> getResults(RequestDto dto) {
        return tenantTx.required(() -> {
            log.debug("[Get Healing Result] Request: {}", dto);
            Set<HealingResultDto> healingResults = healingService.getHealingResults(dto);
            log.debug("[Get Healing Result] Response: {}", healingResults);
            return healingResults;
        });
    }

    /**
     * Setting status of healing
     */
    @PostMapping("/healing/success")
    public void successHealing(@Valid @RequestBody RecordDto.ReportRecord dto) {
        tenantTx.required(() -> {
            log.debug("[Set Healing Status] Request: {}", dto);
            healingService.saveSuccessHealing(dto);
        });
    }

    /**
     * Getting all selectors for selector.html
     */
    @GetMapping({"/selectors", "/selectors/"})
    public ModelAndView get() {
        return tenantTx.required(() -> {
            log.debug("[Get Selector Page]");
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("selector");
            modelAndView.addObject("dto", selectorService.getAllSelectors());
            return modelAndView;
        });
    }

    @GetMapping("/selector/all")
    public List<SelectorRequestDto> getAll() {
        return tenantTx.required(() -> {
            log.debug("[Get All Selectors]");
            return selectorService.getAllSelectors();
        });
    }

    /**
     * Setting status (enable/disable) to healing
     */
    @PostMapping("/selector/status")
    public void setSelectorStatus(@Valid @RequestBody SelectorDto dto) {
        tenantTx.required(() -> {
            log.debug("[Set Selector Status] Request: {}", dto);
            selectorService.setSelectorStatus(dto);
        });
    }

    @GetMapping("/migrate")
    public ModelAndView migrate() {
        return tenantTx.required(() -> {
            log.debug("[Migrate Selectors]");
            selectorService.migrate();
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("index");
            modelAndView.addObject("message", "The migration of selectors was successful.");
            return modelAndView;
        });
    }

    @PostMapping("/selector/save/path")
    public void saveSelectorFilePath(@Valid @RequestBody RecordDto dto) {
        tenantTx.required(() -> {
            log.debug("[Set Selector File Path] Request: {}", dto);
            selectorService.saveSelectorFilePath(dto);
        });
    }

    @PostMapping("/candidate")
    public ResponseEntity<List<Locator>> getCandidate(@RequestBody RequestDto dto) {
        return tenantTx.required(() -> {
            log.debug("[Get Candidate] Request: {})", dto);

            ReferenceElementsDto referenceElements = selectorService.getReferenceElements(dto);
            if (healingService.validateReference(referenceElements)) {
                List<Locator> candidates = healingService.getCandidates(dto, referenceElements).stream()
                        .map(c -> new Locator().setType("css selector")
                                .setValue((String) ((By.ByCssSelector) c).getRemoteParameters().value()))
                        .collect(Collectors.toList());
                log.info("[Get Candidate] Candidates: {})", candidates);
                return ResponseEntity.ok(candidates);
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }

    @PostMapping("/selector-candidates")
    public ResponseEntity<List<SelectorCandidate>> getElementCandidates(@RequestBody RequestDto dto) {
        return tenantTx.required(() -> {
            log.debug("[Get Selector Candidates] Request: {})", dto);

            ReferenceElementsDto referenceElements = selectorService.getReferenceElements(dto);
            if (selectorCandidateService.validateReference(referenceElements)) {
                List<SelectorCandidate> candidates = selectorCandidateService.getCandidates(dto, referenceElements);
                log.info("[Get Selector Candidates] Candidates: {})", candidates);
                return ResponseEntity.ok(candidates);
            } else {
                return ResponseEntity.notFound().build();
            }
        });
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

}