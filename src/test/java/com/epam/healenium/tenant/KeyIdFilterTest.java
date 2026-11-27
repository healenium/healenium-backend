package com.epam.healenium.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class KeyIdFilterTest {

    private KeyIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new KeyIdFilter();
        KeyContext.clear();
    }

    @AfterEach
    void tearDown() {
        KeyContext.clear();
    }

    @Test
    void testValidKeyHeaderIsVisibleInChainAndClearedAfter() throws ServletException, IOException {
        UUID expectedKeyId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/healenium/healing");
        request.addHeader(KeyIdFilter.KEY_HEADER, expectedKeyId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<UUID> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(KeyContext.getKeyId());

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals(expectedKeyId, captured.get());
        assertNull(KeyContext.getKeyId(), "KeyContext must be cleared after filter");
    }

    @Test
    void testMissingKeyHeaderMeansNullAndCallPasses() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/healenium/healing");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<UUID> captured = new AtomicReference<>(UUID.randomUUID());
        FilterChain chain = (req, res) -> captured.set(KeyContext.getKeyId());

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNull(captured.get());
    }

    @Test
    void testInvalidKeyHeaderMeansNullAndDoesNotFailCall() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/healenium/healing");
        request.addHeader(KeyIdFilter.KEY_HEADER, "not-a-valid-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<UUID> captured = new AtomicReference<>(UUID.randomUUID());
        FilterChain chain = (req, res) -> captured.set(KeyContext.getKeyId());

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNull(captured.get());
        assertNull(KeyContext.getKeyId());
    }

    @Test
    void testKeyContextClearedWhenChainFails() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/healenium/healing");
        request.addHeader(KeyIdFilter.KEY_HEADER, UUID.randomUUID().toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            throw new IllegalStateException("boom");
        };

        assertThrows(IllegalStateException.class, () -> filter.doFilter(request, response, chain));
        assertNull(KeyContext.getKeyId(), "KeyContext must be cleared even when the chain fails");
    }
}
