package com.epam.healenium.tenant.membership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipInternalControllerTest {

    private static final UUID TENANT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private MembershipResolutionService membershipResolutionService;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private MembershipInternalController controller;

    @Test
    void returnsTenantsAndDefault() {
        when(membershipResolutionService.resolveTenantIds("iss", "sub")).thenReturn(List.of(TENANT));

        ResponseEntity<Map<String, Object>> response = controller.resolve("iss", "sub");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("defaultTenantId", TENANT.toString());
        assertThat(response.getBody().get("tenants")).isEqualTo(List.of(TENANT.toString()));
    }

    @Test
    void emptyMembershipHasBlankDefault() {
        when(membershipResolutionService.resolveTenantIds("iss", "sub")).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = controller.resolve("iss", "sub");

        assertThat(response.getBody()).containsEntry("defaultTenantId", "");
        assertThat(response.getBody().get("tenants")).isEqualTo(List.of());
    }

    @Test
    void upsertDelegatesToService() {
        MembershipUpsertRequest request = new MembershipUpsertRequest();
        request.setIssuer("iss");
        request.setExternalSub("sub");
        request.setTenantId(TENANT);

        Membership saved = new Membership()
                .setId(UUID.randomUUID())
                .setIssuer("iss")
                .setExternalSub("sub")
                .setTenantId(TENANT)
                .setRole("owner");
        when(membershipService.upsert(request)).thenReturn(saved);

        ResponseEntity<Map<String, Object>> response = controller.upsert(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("tenantId", TENANT.toString());
        assertThat(response.getBody()).containsEntry("role", "owner");
    }

    @Test
    void deleteReturnsNoContentWhenRemoved() {
        when(membershipService.delete("iss", "sub", TENANT)).thenReturn(true);

        ResponseEntity<Void> response = controller.delete("iss", "sub", TENANT);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(membershipService).delete("iss", "sub", TENANT);
    }

    @Test
    void deleteReturnsNotFoundWhenMissing() {
        when(membershipService.delete("iss", "sub", TENANT)).thenReturn(false);

        ResponseEntity<Void> response = controller.delete("iss", "sub", TENANT);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
