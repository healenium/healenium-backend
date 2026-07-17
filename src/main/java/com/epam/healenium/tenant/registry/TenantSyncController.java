package com.epam.healenium.tenant.registry;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Internal control-plane sync from healenium-ai into local {@code tenants}.
 * Skipped by {@link com.epam.healenium.tenant.TenantFilter}; protect with M2M
 * ({@code Healenium-Internal-Token}) when the token is configured.
 */
@RestController
@Profile("pro")
@RequestMapping("/internal/tenants")
@RequiredArgsConstructor
public class TenantSyncController {

    private final TenantSyncService tenantSyncService;

    @PutMapping
    public ResponseEntity<Map<String, Object>> upsert(@Valid @RequestBody TenantSyncRequest request) {
        Tenant saved = tenantSyncService.upsert(request);
        return ResponseEntity.ok(toBody(saved));
    }

    /**
     * One-shot bootstrap / resync of many tenants (A3).
     */
    @PutMapping("/bulk")
    public ResponseEntity<Map<String, Object>> upsertBulk(@Valid @RequestBody List<TenantSyncRequest> requests) {
        List<Tenant> saved = tenantSyncService.upsertAll(requests);
        return ResponseEntity.ok(Map.of(
                "count", saved.size(),
                "tenants", saved.stream().map(this::toBody).toList()
        ));
    }

    private Map<String, Object> toBody(Tenant saved) {
        return Map.of(
                "id", saved.getId().toString(),
                "name", saved.getName(),
                "status", saved.getStatus()
        );
    }

    @ExceptionHandler(InvalidTenantStatusException.class)
    public ResponseEntity<Map<String, String>> badStatus(InvalidTenantStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
