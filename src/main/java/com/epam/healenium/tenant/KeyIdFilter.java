package com.epam.healenium.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Resolves the key id from the {@value #KEY_HEADER} header, set by hlm-iam.
 * A missing or invalid value means null (unknown origin) and never fails the call.
 */
@Slf4j
public class KeyIdFilter extends OncePerRequestFilter {

    public static final String KEY_HEADER = "Healenium-Key-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            KeyContext.setKeyId(parseKeyId(request.getHeader(KEY_HEADER)));
            filterChain.doFilter(request, response);
        } finally {
            KeyContext.clear();
        }
    }

    private UUID parseKeyId(String rawKeyId) {
        if (!StringUtils.hasText(rawKeyId)) {
            return null;
        }
        try {
            return UUID.fromString(rawKeyId.trim());
        } catch (IllegalArgumentException e) {
            log.debug("Ignoring invalid UUID in {} header", KEY_HEADER);
            return null;
        }
    }
}
