package com.epam.healenium.controller;

import com.epam.healenium.tenant.membership.MembershipResolutionService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Profile("pro")
@RestController
@RequestMapping("/healenium")
public class MeController {

    private final MembershipResolutionService membershipResolutionService;

    public MeController(MembershipResolutionService membershipResolutionService) {
        this.membershipResolutionService = membershipResolutionService;
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String issuer = jwt.getClaimAsString("iss");
        String sub = jwt.getSubject();
        if (!StringUtils.hasText(issuer) || !StringUtils.hasText(sub)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT must contain iss and sub claims");
        }
        List<UUID> tenants = membershipResolutionService.resolveTenantIds(issuer, sub);
        UUID defaultTenantId = tenants.size() == 1 ? tenants.get(0) : null;
        return ResponseEntity.ok(new MeResponse(tenants, defaultTenantId));
    }

    public record MeResponse(List<UUID> tenants, UUID defaultTenantId) {
    }
}
