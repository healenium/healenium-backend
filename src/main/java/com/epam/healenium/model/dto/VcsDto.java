package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class VcsDto {
    private String id;
    private String name;

    @Size(max = 50, message = "Unsupported provider: ${validatedValue}")
    @Pattern(regexp = "github|gitlab|azure-devops|gitea", message = "Unsupported provider: ${validatedValue}")
    private String provider;

    @Size(max = 512, message = "baseUrl is not a valid URL")
    private String baseUrl;

    private String accessToken;

    @Pattern(regexp = "^[a-zA-Z0-9._/-]+$", message = "Repository name contains invalid characters")
    private String repository;

    @Pattern(regexp = "^[a-zA-Z0-9._/-]+$", message = "Branch name contains invalid characters")
    private String branch;

    private boolean isActive;
    private LocalDateTime createdDate;

    /**
     * Checks the provider and base URL that will be stored.
     * A null base URL keeps the stored value, so callers pass that stored value in.
     */
    public static void validateProviderAndBaseUrl(String provider, String baseUrl) {
        if (provider != null && !provider.isBlank()
                && (provider.length() > 50 || !isSupportedProvider(provider))) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
        if (baseUrl != null && !baseUrl.isBlank() && (baseUrl.length() > 512 || !isHttpUrl(baseUrl))) {
            throw new IllegalArgumentException("baseUrl is not a valid URL");
        }
        if ("gitea".equals(provider) && (baseUrl == null || baseUrl.isBlank())) {
            throw new IllegalArgumentException("baseUrl is required for provider: gitea");
        }
    }

    private static boolean isSupportedProvider(String provider) {
        return "github".equals(provider)
                || "gitlab".equals(provider)
                || "azure-devops".equals(provider)
                || "gitea".equals(provider);
    }

    private static boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (java.net.URISyntaxException ex) {
            return false;
        }
    }
}

