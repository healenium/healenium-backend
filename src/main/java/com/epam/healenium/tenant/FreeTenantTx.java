package com.epam.healenium.tenant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Free (SINGLE tenant) transaction wrapper.
 * No RLS session variables are set.
 */
@Component
@Profile("free")
public class FreeTenantTx implements TenantTxFacade {

    @Override
    public <T> T required(Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public void required(Runnable runnable) {
        runnable.run();
    }
}
