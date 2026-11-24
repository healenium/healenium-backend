package com.epam.healenium.service.selector;

public interface SelectorIdStrategy {
    String getSelectorId(String locator, String url, String command, boolean urlForKey);
}
