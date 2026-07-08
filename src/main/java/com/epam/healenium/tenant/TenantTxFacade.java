package com.epam.healenium.tenant;

import java.util.function.Supplier;

public interface TenantTxFacade {

    <T> T required(Supplier<T> supplier);

    void required(Runnable runnable);
}
