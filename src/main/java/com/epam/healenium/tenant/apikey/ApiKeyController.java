package com.epam.healenium.tenant.apikey;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal API key management — skipped by TenantFilter (/internal prefix),
 * protected by M2mAuthFilter (Healenium-Internal-Token required).
 */
@RestController
@Profile("pro")
@RequestMapping("/internal/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyResponse> create(@Valid @RequestBody ApiKeyCreateRequest request) {
        ApiKeyResponse response = apiKeyService.create(request.getTenantId(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(apiKeyService.list(tenantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resolves a tenant ID from an API key string. Called by healenium-proxy during WebDriver session init.
     * Returns 404 if the key does not exist or is DISABLED.
     */
    @GetMapping("/resolve")
    public ResponseEntity<Map<String, String>> resolve(@RequestParam String key) {
        Optional<UUID> tenantId = apiKeyService.resolve(key);
        return tenantId
                .map(id -> ResponseEntity.ok(Map.of("tenantId", id.toString())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
