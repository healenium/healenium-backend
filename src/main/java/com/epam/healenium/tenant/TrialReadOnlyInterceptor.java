package com.epam.healenium.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Blocks non-GET requests for tenants whose subscription has expired (DISABLED status).
 * Returns HTTP 402 so clients can prompt the user to renew.
 * Applied only to /healenium/** — internal and actuator paths are excluded.
 */
public class TrialReadOnlyInterceptor implements HandlerInterceptor {

    private static final String RESPONSE_BODY =
            "{\"error\":\"subscription_expired\",\"message\":\"Your subscription has expired. Renew to perform write operations.\"}";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        if (TenantContext.isReadOnly() && !"GET".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(RESPONSE_BODY);
            return false;
        }
        return true;
    }
}
