package com.epam.healenium.model;

import java.io.Serializable;

/**
 * Immutable locator value; JSON maps {@code {"value","type"}} to record components.
 * Requires {@code -parameters} on compile so Jackson 3 binds JSON property names to components.
 * {@code getValue()}/{@code getType()} exist for MapStruct and existing call sites.
 */
public record Locator(String value, String type) implements Serializable {

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
