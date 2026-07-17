package com.epam.healenium.tenant.registry;

public class InvalidTenantStatusException extends RuntimeException {

    public InvalidTenantStatusException(String message) {
        super(message);
    }
}
