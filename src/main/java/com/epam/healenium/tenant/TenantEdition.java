package com.epam.healenium.tenant;

/**
 * Runtime edition flag set once at startup from Spring profile ({@code pro} vs {@code free}).
 * Used by JPA entity listeners that are not Spring-managed beans.
 */
public final class TenantEdition {

    private static volatile boolean pro;

    private TenantEdition() {
    }

    public static boolean isPro() {
        return pro;
    }

    static void setPro(boolean proEdition) {
        pro = proEdition;
    }
}
