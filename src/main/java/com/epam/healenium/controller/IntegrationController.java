package com.epam.healenium.controller;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.dto.LlmModelDto;
import com.epam.healenium.model.dto.elitea.DedicatedInfo;
import com.epam.healenium.model.dto.elitea.LlmDto;
import com.epam.healenium.model.dto.elitea.LocatorPathsDto;
import com.epam.healenium.model.dto.elitea.VcsDto;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.service.IntegrationService;
import com.epam.healenium.service.LlmModelService;
import com.epam.healenium.service.ReportService;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.tenant.TenantTxFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

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
                throw new IllegalArgumentException("Report ID cannot be null or empty");
            }

            boolean hasRecords = reportRepository.findById(reportId)
                    .map(Report::getRecordWrapper)
                    .map(wrapper -> !wrapper.getRecords().isEmpty())
                    .orElse(false);

            boolean availableForSd = integrationService.isAvailableForSd() && hasRecords;
            boolean availableForMr = availableForSd && reportService.getDedicatedInfo(reportId).isEmpty();

            DedicatedInfo dedicatedInfo = new DedicatedInfo()
                    .setAvailableForSd(availableForSd)
                    .setAvailableForMr(availableForMr);

            return ResponseEntity.ok(dedicatedInfo);
        });
    }

    @PatchMapping("/paths/save/{reportId}")
    public ResponseEntity<DedicatedInfo> saveLocatorPaths(@PathVariable String reportId, 
                                                         @Valid @RequestBody List<LocatorPathsDto> detectionPaths) {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Saving locator paths for report: {}, paths: {}", 
                    reportId, detectionPaths.stream().map(LocatorPathsDto::getSelectedPath).toList());

            if (reportId == null || reportId.trim().isEmpty()) {
                throw new IllegalArgumentException("Report ID cannot be null or empty");
            }

            if (detectionPaths.isEmpty()) {
                throw new IllegalArgumentException("Detection paths cannot be null or empty");
            }

            selectorService.saveLocatorPaths(detectionPaths, reportId);
            List<HealingResult> invalidSelectorClass = reportService.getDedicatedInfo(reportId);

            DedicatedInfo dedicatedInfo = new DedicatedInfo()
                    .setAvailableForMr(invalidSelectorClass.isEmpty());

            return ResponseEntity.ok(dedicatedInfo);
        });
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

    @GetMapping("/vcs/ui")
    public ResponseEntity<List<VcsDto>> getVcsList() {
        return tenantTx.required(() -> {
            log.debug("[Integration] Get vcs list");
            return ResponseEntity.ok(integrationService.getVcsAll());
        });
    }

    @GetMapping("/vcs/{platform}")
    public ResponseEntity<VcsDto> getVcs(@PathVariable String platform) {
        return tenantTx.required(() -> {
            log.debug("[Integration] Get vcs, platform: {}", platform);
            VcsDto vcsDto = integrationService.getVcs(platform);
            return ResponseEntity.ok(vcsDto);
        });
    }

    @PatchMapping("/vcs/{id}")
    public ResponseEntity<List<VcsDto>> activateVcs(@PathVariable String id, @RequestBody VcsDto vcsDto) {
        return tenantTx.required(() -> {
            if (vcsDto == null || !vcsDto.isActive()) {
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
// get all
    @GetMapping("/llm/ui")
    public ResponseEntity<List<LlmDto>> getLlmList() {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Get llm list");
            return ResponseEntity.ok(integrationService.getLlmAll());
        });
    }
// get active
    @GetMapping("/llm/ai")
    public ResponseEntity<LlmDto> getLlmForAi() {
        return tenantTx.required(() -> {
            log.info("[SETTINGS] Get active llm for ai");
            return ResponseEntity.ok(integrationService.getLlm("ai"));
        });
    }

    @PatchMapping("/llm/{id}")
    public ResponseEntity<List<LlmDto>> activateLlm(@PathVariable String id, @RequestBody LlmDto llmDto) {
        return tenantTx.required(() -> {
            if (llmDto == null || !llmDto.isActive()) {
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
        log.info("[SETTINGS] Get llm-models for ui");
        return tenantTx.required(() -> ResponseEntity.ok(llmModelService.list()));
    }

}
