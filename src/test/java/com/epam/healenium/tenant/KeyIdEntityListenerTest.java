package com.epam.healenium.tenant;

import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KeyIdEntityListenerTest {

    private final KeyIdEntityListener listener = new KeyIdEntityListener();

    @BeforeEach
    void setUp() {
        KeyContext.clear();
    }

    @AfterEach
    void tearDown() {
        KeyContext.clear();
    }

    @Test
    void testStampsReportHealingAndHealingResultFromContext() {
        UUID keyId = UUID.randomUUID();
        KeyContext.setKeyId(keyId);

        Report report = new Report();
        Healing healing = new Healing();
        HealingResult result = new HealingResult();

        listener.prePersist(report);
        listener.prePersist(healing);
        listener.prePersist(result);

        assertEquals(keyId, report.getKeyId());
        assertEquals(keyId, healing.getKeyId());
        assertEquals(keyId, result.getKeyId());
    }

    @Test
    void testWithoutContextKeyIdStaysNull() {
        Report report = new Report();

        listener.prePersist(report);

        assertNull(report.getKeyId());
    }

    @Test
    void testExistingKeyIdIsNotOverwritten() {
        UUID existing = UUID.randomUUID();
        KeyContext.setKeyId(UUID.randomUUID());
        Report report = new Report().setKeyId(existing);

        listener.prePersist(report);

        assertEquals(existing, report.getKeyId());
    }
}
