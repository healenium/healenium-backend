package com.epam.healenium.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantFilterTest {

    private TenantFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TenantFilter();
        TenantContext.clear();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        MDC.clear();
    }

    @Test
    void testValidTenantIdHeader() throws ServletException, IOException {
        UUID expectedTenantId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/selector");
        request.addHeader(TenantFilter.TENANT_HEADER, expectedTenantId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<UUID> capturedTenantId = new AtomicReference<>();
        AtomicReference<String> capturedMdc = new AtomicReference<>();

        FilterChain filterChain = (req, res) -> {
            capturedTenantId.set(TenantContext.getTenantId());
            capturedMdc.set(MDC.get("tenant_id"));
        };

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals(expectedTenantId, capturedTenantId.get());
        assertEquals(expectedTenantId.toString(), capturedMdc.get());
        assertNull(TenantContext.getTenantId(), "TenantContext must be cleared after filter");
        assertNull(MDC.get("tenant_id"), "MDC must be cleared after filter");
    }

    @Test
    void testInvalidTenantIdHeaderReturns400() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/selector");
        request.addHeader(TenantFilter.TENANT_HEADER, "not-a-valid-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(400, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void testMissingTenantIdHeaderDefaultsToDefaultTenant() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/selector");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<UUID> capturedTenantId = new AtomicReference<>();

        FilterChain filterChain = (req, res) -> {
            capturedTenantId.set(TenantContext.getTenantId());
        };

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals(TenantFilter.DEFAULT_TENANT_ID, capturedTenantId.get());
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void testSkipPathShouldNotFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(TenantContext.getTenantId());
    }
}
