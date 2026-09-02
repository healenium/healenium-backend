package com.epam.healenium.tenant.membership;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Internal API for hlm-proxy membership resolution and control-plane provisioning.
 * Protect with {@code Healenium-Internal-Token} when configured.
 */
@RestController
@Profile("pro")
@RequestMapping("/internal/membership")
@RequiredArgsConstructor
public class MembershipInternalController {

    private final MembershipResolutionService membershipResolutionService;
    private final MembershipService membershipService;
    private final UserProvisioningService userProvisioningService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> resolve(@RequestParam @NotBlank String issuer,
                                                       @RequestParam @NotBlank String sub) {
        List<UUID> tenants = membershipResolutionService.resolveTenantIds(issuer.trim(), sub.trim());
        UUID defaultTenantId = tenants.isEmpty() ? null : tenants.getFirst();
        return ResponseEntity.ok(Map.of(
                "issuer", issuer.trim(),
                "sub", sub.trim(),
                "tenants", tenants.stream().map(UUID::toString).toList(),
                "defaultTenantId", defaultTenantId != null ? defaultTenantId.toString() : ""
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> upsert(@Valid @RequestBody MembershipUpsertRequest request) {
        Membership saved = membershipService.upsert(request);
        return ResponseEntity.ok(Map.of(
                "id", saved.getId().toString(),
                "issuer", saved.getIssuer(),
                "externalSub", saved.getExternalSub(),
                "tenantId", saved.getTenantId().toString(),
                "role", saved.getRole() != null ? saved.getRole() : ""
        ));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam @NotBlank String issuer,
                                       @RequestParam @NotBlank String sub,
                                       @RequestParam @NotNull UUID tenantId) {
        boolean removed = membershipService.delete(issuer, sub, tenantId);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Idempotent user provisioning: creates a tenant + membership on first call,
     * returns the existing tenant on subsequent calls.
     * Called by hlm-proxy/MeController when /me returns empty tenants.
     */
    @PostMapping("/provision")
    public ResponseEntity<Map<String, String>> provision(@RequestParam @NotBlank String issuer,
                                                         @RequestParam @NotBlank String sub) {
        UUID tenantId = userProvisioningService.provisionIfNeeded(issuer.trim(), sub.trim());
        return ResponseEntity.ok(Map.of("tenantId", tenantId.toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
