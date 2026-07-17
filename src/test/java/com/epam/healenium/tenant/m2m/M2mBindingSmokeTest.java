package com.epam.healenium.tenant.m2m;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("pro")
class M2mBindingSmokeTest {

    @Autowired
    private M2mAuthProperties properties;

    @DynamicPropertySource
    static void emptyEnvVarOnly(DynamicPropertyRegistry registry) {
        registry.add("M2M_API_KEYS", () -> "");
    }

    @Test
    void emptyM2mApiKeysEnvOverridesYamlDefault() {
        // ${M2M_API_KEYS:default} resolves to "" when env is set but empty
        assertThat(properties.getApiKeys()).isEmpty();
        assertThat(properties.getInternalToken()).isEmpty();
    }
}
