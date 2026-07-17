package com.epam.healenium.tenant.m2m;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "healenium.m2m")
public class M2mAuthProperties {

    private List<String> apiKeys = new ArrayList<>();

    /**
     * Shared secret set by hlm-proxy / healenium-ai ({@code Healenium-Internal-Token}).
     * Required for Pro callers that do not use an API key.
     */
    private String internalToken = "";

    public List<String> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<String> apiKeys) {
        this.apiKeys = apiKeys != null ? apiKeys : new ArrayList<>();
    }

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken != null ? internalToken : "";
    }
}
