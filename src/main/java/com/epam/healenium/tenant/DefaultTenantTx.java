package com.epam.healenium.tenant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Universal tenant transaction wrapper.
 * Executes transaction-scoped SET LOCAL so Postgres RLS enforces tenant isolation.
 * Automatically falls back to {@link TenantFilter#DEFAULT_TENANT_ID} if no tenant context is set.
 */
@Component
public class DefaultTenantTx implements TenantTxFacade {

    private final JdbcTemplate jdbcTemplate;

    public DefaultTenantTx(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T required(Supplier<T> supplier) {
        setTenant();
        return supplier.get();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void required(Runnable runnable) {
        setTenant();
        runnable.run();
    }

    private void setTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = TenantFilter.DEFAULT_TENANT_ID;
        }
        jdbcTemplate.execute("SET LOCAL app.tenant_id = '" + tenantId + "'");
    }
}
