package com.epam.healenium.tenant;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantFilterTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private TenantValidationService validationService;
    private TenantFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        validationService = mock(TenantValidationService.class);
        filter = new TenantFilter(validationService);
        chain = mock(FilterChain.class);
    }

    @Test
    void rejectsMissingHeader() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/healenium/healing"), response, chain);

        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void rejectsInvalidUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/healing");
        request.addHeader(TenantFilter.TENANT_HEADER, "not-a-uuid");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void rejectsInactiveTenant() throws Exception {
        when(validationService.isTenantAllowed(TENANT_ID)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/healing");
        request.addHeader(TenantFilter.TENANT_HEADER, TENANT_ID.toString());

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void allowsActiveTenant() throws Exception {
        when(validationService.isTenantAllowed(TENANT_ID)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/healing");
        request.addHeader(TenantFilter.TENANT_HEADER, TENANT_ID.toString());

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void skipsInternalPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/internal/tenants");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(validationService, never()).isTenantAllowed(org.mockito.ArgumentMatchers.any());
    }
}
