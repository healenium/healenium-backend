package com.epam.healenium.tenant;

import java.util.UUID;

/**
 * Stores per-request key id: the iam api key that made the call, or null when unknown.
 *
 * NOTE: Must be cleared after each request to avoid cross-request leakage.
 */
public final class KeyContext {

    private static final ThreadLocal<UUID> KEY_ID = new ThreadLocal<>();

    private KeyContext() {
    }

    public static UUID getKeyId() {
        return KEY_ID.get();
    }

    public static void setKeyId(UUID keyId) {
        KEY_ID.set(keyId);
    }

    public static void clear() {
        KEY_ID.remove();
    }
}
