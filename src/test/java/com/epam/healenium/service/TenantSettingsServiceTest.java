package com.epam.healenium.service;

import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.model.domain.Settings;
import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.repository.SettingsRepository;
import com.epam.healenium.service.impl.TenantSettingsServiceImpl;
import com.epam.healenium.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantSettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private DynamicSettings dynamicSettings;

    private TenantSettingsServiceImpl service;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TenantSettingsServiceImpl(settingsRepository, dynamicSettings);
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testGetSettingsReturnsExistingEntity() {
        Settings entity = new Settings()
                .setId(UUID.randomUUID())
                .setTenantId(tenantId)
                .setHealEnabled(false)
                .setRecoveryTries(3)
                .setScoreCap(0.75)
                .setHealedType("xpath");

        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.of(entity));

        TenantSettingsDto result = service.getSettings();

        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
        assertFalse(result.getHealEnabled());
        assertEquals(3, result.getRecoveryTries());
        assertEquals(0.75, result.getScoreCap());
        assertEquals("xpath", result.getHealedType());
    }

    @Test
    void testGetSettingsReturnsDefaultWhenMissing() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());
        when(dynamicSettings.getRecoveryTries()).thenReturn(1);
        when(dynamicSettings.getScoreCap()).thenReturn(0.60);
        when(dynamicSettings.getSelectorType()).thenReturn("cssSelector");

        TenantSettingsDto result = service.getSettings();

        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
        assertTrue(result.getHealEnabled());
        assertEquals(1, result.getRecoveryTries());
        assertEquals(0.60, result.getScoreCap());
        assertEquals("css", result.getHealedType());
    }

    @Test
    void testUpdateSettingsCreatesNewIfAbsent() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantSettingsDto request = new TenantSettingsDto()
                .setHealEnabled(false)
                .setRecoveryTries(2)
                .setScoreCap(0.85)
                .setHealedType("xpath");

        TenantSettingsDto updated = service.updateSettings(request);

        assertNotNull(updated);
        assertEquals(tenantId, updated.getTenantId());
        assertFalse(updated.getHealEnabled());
        assertEquals(2, updated.getRecoveryTries());
        assertEquals(0.85, updated.getScoreCap());
        assertEquals("xpath", updated.getHealedType());

        ArgumentCaptor<Settings> captor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsRepository).save(captor.capture());
        Settings saved = captor.getValue();
        assertEquals(tenantId, saved.getTenantId());
        assertEquals("xpath", saved.getHealedType());
    }

    @Test
    void testUpdateSettingsNormalizesCssSelectorToCss() {
        when(settingsRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantSettingsDto request = new TenantSettingsDto().setHealedType("cssSelector");
        TenantSettingsDto updated = service.updateSettings(request);

        assertEquals("css", updated.getHealedType());
    }

    @Test
    void testUpdateSettingsRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateSettings(new TenantSettingsDto().setRecoveryTries(0)));

        assertThrows(IllegalArgumentException.class, () ->
                service.updateSettings(new TenantSettingsDto().setScoreCap(1.5)));

        assertThrows(IllegalArgumentException.class, () ->
                service.updateSettings(new TenantSettingsDto().setScoreCap(-0.1)));

        assertThrows(IllegalArgumentException.class, () ->
                service.updateSettings(new TenantSettingsDto().setHealedType("invalid_type")));
    }
}
