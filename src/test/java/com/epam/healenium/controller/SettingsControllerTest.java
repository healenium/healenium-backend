package com.epam.healenium.controller;

import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.service.SettingService;
import com.epam.healenium.service.TenantSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SettingsControllerTest {

    @Mock
    private SettingService settingService;

    @Mock
    private TenantSettingsService tenantSettingsService;

    @InjectMocks
    private SettingsController controller;

    private MockMvc mockMvc;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetSettings() throws Exception {
        TenantSettingsDto dto = new TenantSettingsDto()
                .setTenantId(tenantId)
                .setHealEnabled(true)
                .setRecoveryTries(2)
                .setScoreCap(0.65)
                .setHealedType("xpath")
                .setKeySelectorUrl(true)
                .setCollectMetrics(true)
                .setFindElementsAutoHealing(false);

        when(tenantSettingsService.getSettings()).thenReturn(dto);

        mockMvc.perform(get("/healenium/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.healEnabled").value(true))
                .andExpect(jsonPath("$.recoveryTries").value(2))
                .andExpect(jsonPath("$.scoreCap").value(0.65))
                .andExpect(jsonPath("$.healedType").value("xpath"))
                .andExpect(jsonPath("$.keySelectorUrl").value(true))
                .andExpect(jsonPath("$.collectMetrics").value(true))
                .andExpect(jsonPath("$.findElementsAutoHealing").value(false))
                // Uppercase aliases for backward compatibility
                .andExpect(jsonPath("$.KEY_SELECTOR_URL").value(true))
                .andExpect(jsonPath("$.COLLECT_METRICS").value(true))
                .andExpect(jsonPath("$.SCORE_CAP").value(0.65))
                .andExpect(jsonPath("$.SELECTOR_TYPE").value("xpath"))
                .andExpect(jsonPath("$.RECOVERY_TRIES").value(2));
    }

    @Test
    void testPutSettings() throws Exception {
        String jsonPayload = "{\"healEnabled\":false,\"recoveryTries\":3,\"scoreCap\":0.70,\"healedType\":\"css\"}";

        TenantSettingsDto response = new TenantSettingsDto()
                .setTenantId(tenantId)
                .setHealEnabled(false)
                .setRecoveryTries(3)
                .setScoreCap(0.70)
                .setHealedType("css");

        when(tenantSettingsService.updateSettings(any(TenantSettingsDto.class))).thenReturn(response);

        mockMvc.perform(put("/healenium/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.healEnabled").value(false))
                .andExpect(jsonPath("$.recoveryTries").value(3))
                .andExpect(jsonPath("$.scoreCap").value(0.70))
                .andExpect(jsonPath("$.healedType").value("css"));
    }

    @Test
    void testPostUpdateWithJsonBody() throws Exception {
        String jsonPayload = "{\"key\":\"SCORE_CAP\",\"value\":\"0.8\"}";
        when(settingService.updateSetting(eq("SCORE_CAP"), eq("0.8")))
                .thenReturn(Collections.singletonMap("success", true));

        mockMvc.perform(post("/healenium/settings/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(settingService).updateSetting("SCORE_CAP", "0.8");
    }

    @Test
    void testPostUpdateWithQueryParams() throws Exception {
        when(settingService.updateSetting(eq("SCORE_CAP"), eq("0.85")))
                .thenReturn(Collections.singletonMap("success", true));

        mockMvc.perform(post("/healenium/settings/update")
                        .param("key", "SCORE_CAP")
                        .param("value", "0.85"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(settingService).updateSetting("SCORE_CAP", "0.85");
    }
}

