package com.epam.healenium.controller;

import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.dto.LlmModelDto;
import com.epam.healenium.model.dto.DedicatedInfo;
import com.epam.healenium.model.dto.LlmDto;
import com.epam.healenium.model.dto.LocatorPathsDto;
import com.epam.healenium.model.dto.VcsDto;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.service.IntegrationService;
import com.epam.healenium.service.LlmModelService;
import com.epam.healenium.service.ReportService;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.tenant.TenantTxFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/healenium/integration")
public class IntegrationController {

    private final TenantTxFacade tenantTx;
    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final SelectorService selectorService;
    private final IntegrationService integrationService;
    private final LlmModelService llmModelService;

    public IntegrationController(TenantTxFacade tenantTx, ReportService reportService, ReportRepository reportRepository, SelectorService selectorService, IntegrationService integrationService, LlmModelService llmModelService) {
        this.tenantTx = tenantTx;
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.selectorService = selectorService;
        this.integrationService = integrationService;
        this.llmModelService = llmModelService;
    }

    @GetMapping("/dedicated-info/{reportId}")
    public ResponseEntity<DedicatedInfo> getDedicatedInfo(@PathVariable String reportId) {
        return tenantTx.required(() -> {
            log.debug("[Integration] Get dedicated-info, reportId: {}", reportId);
            if (reportId == null || reportId.trim().isEmpty()) {
                log.warn("[Integration] Get dedicated-info rejected: Report ID cannot be null or empty");
                throw new IllegalArgumentException("Report ID cannot be null or empty");
            }

            return ResponseEntity.ok(dedicatedInfo(reportId));
        });
    }

    @PatchMapping("/paths/save/{reportId}")
    public ResponseEntity<DedicatedInfo> saveLocatorPaths(@PathVariable String reportId, 
                                                         @Valid @RequestBody List<LocatorPathsDto> detectionPaths) {
        return tenantTx.required(() -> {
            if (reportId == null || reportId.trim().isEmpty()) {
                log.warn("[SETTINGS] Saving locator paths rejected: Report ID cannot be null or empty");
                throw new IllegalArgumentException("Report ID cannot be null or empty");
            }

            if (detectionPaths.isEmpty()) {
                log.warn("[SETTINGS] Saving locator paths rejected for report {}: Detection paths cannot be null or empty", reportId);
                throw new IllegalArgumentException("Detection paths cannot be null or empty");
            }

            log.info("[SETTINGS] Saving locator paths for report: {}", reportId);
            log.debug("[SETTINGS] Locator paths for report {}: {}",
                    reportId, detectionPaths.stream().map(LocatorPathsDto::getSelectedPath).toList());

            selectorService.saveLocatorPaths(detectionPaths, reportId);
            return ResponseEntity.ok(dedicatedInfo(reportId));
        });
    }

    private DedicatedInfo dedicatedInfo(String reportId) {
        boolean hasRecords = reportRepository.findById(reportId)
                .map(Report::getRecordWrapper)
                .map(wrapper -> !wrapper.getRecords().isEmpty())
                .orElse(false);
        return new DedicatedInfo()
                .setHasActiveGit(integrationService.hasActiveGit())
                .setHasActiveGenAI(integrationService.hasActiveGenAI())
                .setHasRecords(hasRecords)
                .setNoHealingResult(reportService.getDedicatedInfo(reportId).isEmpty());
    }

    @PostMapping("/vcs")
    public ResponseEntity<List<VcsDto>> createVcs(@Valid @RequestBody VcsDto vcsDto) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Creating VCS configuration: {}", vcsDto.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(integrationService.createVcs(vcsDto));
        });
    }

    @PutMapping("/vcs/{id}")
    public ResponseEntity<List<VcsDto>> updateVcs(@PathVariable String id, @Valid @RequestBody VcsDto vcsDto) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Updating VCS configuration: {}", id);
            return ResponseEntity.ok(integrationService.updateVcs(id, vcsDto));
        });
    }

    @GetMapping("/vcs/all")
    public ResponseEntity<List<VcsDto>> getVcsList() {
        return tenantTx.required(() -> {
            log.debug("[SETTINGS] Get vcs list");
            return ResponseEntity.ok(integrationService.getVcsAll());
        });
    }

    @GetMapping("/vcs/active")
    public ResponseEntity<VcsDto> getVcsActive() {
        return tenantTx.required(() -> {
            log.debug("[SETTINGS] Get active vcs for ai");
            return ResponseEntity.ok(integrationService.getActiveVcs());
        });
    }

    @PatchMapping("/vcs/{id}")
    public ResponseEntity<List<VcsDto>> activateVcs(@PathVariable String id, @RequestBody VcsDto vcsDto) {
        return tenantTx.required(() -> {
            if (vcsDto == null || !vcsDto.isActive()) {
                log.warn("[SETTINGS] Activating VCS rejected for id {}: active must be true", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "active must be true");
            }
            log.info("[SETTINGS] Activating VCS with id: {}", id);
            return ResponseEntity.ok(integrationService.activateVcs(id));
        });
    }

    @DeleteMapping("/vcs/{id}")
    public ResponseEntity<Void> deleteVcs(@PathVariable String id) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Deleting VCS with id: {}", id);
            integrationService.deleteVcs(id);
            return ResponseEntity.ok().build();
        });
    }

    @PostMapping("/llm")
    public ResponseEntity<List<LlmDto>> createLlm(@Valid @RequestBody LlmDto llmDto) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Creating LLM configuration: {}", llmDto.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(integrationService.createLlm(llmDto));
        });
    }

    @PutMapping("/llm/{id}")
    public ResponseEntity<List<LlmDto>> updateLlm(@PathVariable String id, @Valid @RequestBody LlmDto llmDto) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Updating LLM configuration: {}", id);
            return ResponseEntity.ok(integrationService.updateLlm(id, llmDto));
        });
    }

    @GetMapping("/llm/all")
    public ResponseEntity<List<LlmDto>> getLlmList() {
        return tenantTx.required(() -> {
            log.debug("[SETTINGS] Get llm list");
            return ResponseEntity.ok(integrationService.getLlmAll());
        });
    }

    @GetMapping("/llm/active")
    public ResponseEntity<LlmDto> getLlmActive() {
        return tenantTx.required(() -> {
            log.debug("[SETTINGS] Get active llm for ai");
            return ResponseEntity.ok(integrationService.getActiveLlm());
        });
    }

    @PatchMapping("/llm/{id}")
    public ResponseEntity<List<LlmDto>> activateLlm(@PathVariable String id, @RequestBody LlmDto llmDto) {
        return tenantTx.required(() -> {
            if (llmDto == null || !llmDto.isActive()) {
                log.warn("[SETTINGS] Activating LLM rejected for id {}: active must be true", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "active must be true");
            }
            log.info("[SETTINGS] Activating LLM with id: {}", id);
            return ResponseEntity.ok(integrationService.setActiveLlm(id));
        });
    }

    @DeleteMapping("/llm/{id}")
    public ResponseEntity<Void> deleteLlm(@PathVariable String id) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Deleting LLM with id: {}", id);
            integrationService.deleteLlm(id);
            return ResponseEntity.ok().build();
        });
    }

    @GetMapping("/llm-models")
    public ResponseEntity<List<LlmModelDto>> listLlmModels() {
        return tenantTx.required(() -> {
            log.debug("[SETTINGS] Get llm-models for ui");
            return ResponseEntity.ok(llmModelService.list());
        });
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Invalid configuration"
                : ex.getMessage();
        return ResponseEntity.badRequest().body(Map.of("error_message", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        String message = ex.getReason() == null || ex.getReason().isBlank()
                ? ex.getStatusCode().toString()
                : ex.getReason();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error_message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(text -> text != null && !text.isBlank())
                .distinct()
                .collect(Collectors.joining(" "));
        if (message.isBlank()) {
            message = "Invalid configuration";
        }
        return ResponseEntity.badRequest().body(Map.of("error_message", message));
    }

}
