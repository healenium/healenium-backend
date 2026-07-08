package com.epam.healenium.tenant;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Pro (MULTI tenant) transaction wrapper.
 * Executes transaction-scoped SET LOCAL so Postgres RLS can filter rows.
 */
@Component
@Profile("pro")
public class ProTenantTx implements TenantTxFacade {

    private final JdbcTemplate jdbcTemplate;

    public ProTenantTx(JdbcTemplate jdbcTemplate) {
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
            throw new IllegalStateException("Tenant is not set in TenantContext");
        }
        jdbcTemplate.execute("SET LOCAL app.tenant_id = '" + tenantId + "'");
    }
}
