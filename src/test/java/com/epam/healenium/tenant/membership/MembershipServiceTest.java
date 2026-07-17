package com.epam.healenium.tenant.membership;

import com.epam.healenium.tenant.registry.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final UUID TENANT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private MembershipResolutionService membershipResolutionService;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void upsertCreatesMembershipAndInvalidatesCache() {
        when(tenantRepository.existsById(TENANT)).thenReturn(true);
        when(membershipRepository.findByIssuerAndExternalSubAndTenantId("iss", "sub", TENANT))
                .thenReturn(Optional.empty());
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> {
            Membership m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        MembershipUpsertRequest request = new MembershipUpsertRequest();
        request.setIssuer(" iss ");
        request.setExternalSub(" sub ");
        request.setTenantId(TENANT);
        request.setRole("owner");

        Membership saved = membershipService.upsert(request);

        assertThat(saved.getIssuer()).isEqualTo("iss");
        assertThat(saved.getExternalSub()).isEqualTo("sub");
        assertThat(saved.getTenantId()).isEqualTo(TENANT);
        assertThat(saved.getRole()).isEqualTo("owner");

        ArgumentCaptor<Membership> captor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(captor.capture());
        assertThat(captor.getValue().getIssuer()).isEqualTo("iss");
        verify(membershipResolutionService).invalidate("iss", "sub");
    }

    @Test
    void upsertRejectsUnknownTenant() {
        when(tenantRepository.existsById(TENANT)).thenReturn(false);

        MembershipUpsertRequest request = new MembershipUpsertRequest();
        request.setIssuer("iss");
        request.setExternalSub("sub");
        request.setTenantId(TENANT);

        assertThatThrownBy(() -> membershipService.upsert(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant does not exist");
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void deleteInvalidatesCacheWhenRowRemoved() {
        when(membershipRepository.deleteByIssuerAndExternalSubAndTenantId("iss", "sub", TENANT))
                .thenReturn(1);

        assertThat(membershipService.delete("iss", "sub", TENANT)).isTrue();
        verify(membershipResolutionService).invalidate("iss", "sub");
    }

    @Test
    void deleteReturnsFalseWhenMissing() {
        when(membershipRepository.deleteByIssuerAndExternalSubAndTenantId("iss", "sub", TENANT))
                .thenReturn(0);

        assertThat(membershipService.delete("iss", "sub", TENANT)).isFalse();
        verify(membershipResolutionService, never()).invalidate(any(), any());
    }
}
