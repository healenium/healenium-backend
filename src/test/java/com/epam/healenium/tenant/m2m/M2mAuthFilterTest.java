package com.epam.healenium.tenant.m2m;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class M2mAuthFilterTest {

    private M2mAuthProperties properties;
    private M2mAuthFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        properties = new M2mAuthProperties();
        filter = new M2mAuthFilter(properties);
        chain = mock(FilterChain.class);
    }

    @Test
    void rejectsWhenNoCredentialsConfigured() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/healenium/healing"), response, chain);

        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rejectsWithoutMatchingCredential() throws Exception {
        properties.setApiKeys(List.of("mobitru-key"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/healenium/healing"), response, chain);

        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void passesWithValidApiKey() throws Exception {
        properties.setApiKeys(List.of("mobitru-key"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/healing");
        request.addHeader(M2mAuthFilter.API_KEY_HEADER, "mobitru-key");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void passesWithInternalToken() throws Exception {
        properties.setInternalToken("proxy-secret");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healenium/healing");
        request.addHeader(M2mAuthFilter.INTERNAL_TOKEN_HEADER, "proxy-secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void protectsInternalPathWithoutToken() throws Exception {
        properties.setInternalToken("proxy-secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("PUT", "/internal/tenants"), response, chain);

        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void allowsInternalPathWithToken() throws Exception {
        properties.setInternalToken("proxy-secret");

        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/internal/tenants");
        request.addHeader(M2mAuthFilter.INTERNAL_TOKEN_HEADER, "proxy-secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsActuator() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), response, chain);

        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
