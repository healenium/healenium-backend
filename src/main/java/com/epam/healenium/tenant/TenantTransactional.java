package com.epam.healenium.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation. Kept for backward compatibility.
 *
 * Tenant RLS is now configured via {@link TenantTx} which runs code inside an active transaction and
 * executes "SET LOCAL app.tenant_id".
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantTransactional {
}
