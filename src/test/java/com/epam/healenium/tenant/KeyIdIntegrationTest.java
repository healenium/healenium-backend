package com.epam.healenium.tenant;

import com.epam.healenium.initializer.TestContainersInitializer;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.SettingsService;
import com.epam.healenium.service.selector.SelectorIdStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeyIdIntegrationTest extends TestContainersInitializer {

    private static final String DEFAULT_TENANT = TenantFilter.DEFAULT_TENANT_ID.toString();
    private static final String ZERO_UUID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private HealingRepository healingRepository;
    @Autowired
    private HealingResultRepository healingResultRepository;
    @Autowired
    private SelectorRepository selectorRepository;
    @Autowired
    private SelectorIdStrategy selectorIdStrategy;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private TenantTxFacade tenantTx;

    private final List<String> reportIds = new ArrayList<>();
    private final List<String> healingIds = new ArrayList<>();
    private final List<String> selectorIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        tenantTx.required(() -> {
            healingIds.forEach(id -> healingRepository.findById(id).ifPresent(healing -> {
                healing.getResults().forEach(healingResultRepository::delete);
                healingRepository.delete(healing);
            }));
            selectorIds.forEach(selectorRepository::deleteById);
            reportIds.forEach(reportRepository::deleteById);
        });
        reportIds.clear();
        healingIds.clear();
        selectorIds.clear();
    }

    @Test
    void testInitWithKeyHeaderStoresKeyId() throws Exception {
        UUID keyId = UUID.randomUUID();

        String uid = initReport(keyId.toString(), null);

        assertEquals(keyId, findReport(uid).getKeyId());
    }

    @Test
    void testInitWithTenantIdOnlyStoresNullKeyId() throws Exception {
        String uid = initReport(null, DEFAULT_TENANT);

        assertNull(findReport(uid).getKeyId());
    }

    @Test
    void testInvalidKeyHeaderStoresNullKeyIdAndCallSucceeds() throws Exception {
        String uid = initReport("not-a-valid-uuid", null);

        assertNull(findReport(uid).getKeyId());
    }

    @Test
    void testReportListFilterKeepsOnlyMatchingKeyIdsAndNullOrigin() throws Exception {
        UUID keyA = UUID.randomUUID();
        UUID keyB = UUID.randomUUID();
        String reportA = initReport(keyA.toString(), null);
        String reportB = initReport(keyB.toString(), null);
        String reportNull = initReport(null, null);

        mockMvc.perform(get("/healenium/report/all").param("keyIds", keyA.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(reportA)))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(reportB))))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(reportNull))));

        mockMvc.perform(get("/healenium/report/all").param("keyIds", keyA + "," + keyB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItems(reportA, reportB)))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(reportNull))));

        mockMvc.perform(get("/healenium/report/all").param("keyIds", ZERO_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(not(hasItem(reportA))));

        mockMvc.perform(get("/healenium/report/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItems(reportA, reportB, reportNull)));
    }

    @Test
    void testReportListRejectsInvalidKeyIds() throws Exception {
        mockMvc.perform(get("/healenium/report/all").param("keyIds", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHealingFilterOnKeyIdsAndHealingResultsStampedWithKey() throws Exception {
        UUID keyA = UUID.randomUUID();
        String className = "com.epam.healenium.KeyId" + UUID.randomUUID();
        String locator = "//div[@id='key-" + UUID.randomUUID() + "']";
        String url = "https://key-id.test";
        String command = "click";
        boolean urlForKey = settingsService.isKeySelectorUrl();
        String selectorId = selectorIdStrategy.getSelectorId(locator, url, command, urlForKey);
        tenantTx.required(() -> saveSelector(selectorId, className, locator, url, command));

        saveHealing(keyA, selectorId, "page-a", "//p[@id='a']");
        saveHealing(null, selectorId, "page-null", "//p[@id='null']");

        mockMvc.perform(get("/healenium/healing").param("locator", locator).param("keyIds", keyA.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].results[*].locator.value").value(hasItem("//p[@id='a']")))
                .andExpect(jsonPath("$[*].results[*].locator.value").value(not(hasItem("//p[@id='null']"))));

        mockMvc.perform(get("/healenium/healing").param("locator", locator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].results[*].locator.value").value(hasItems("//p[@id='a']", "//p[@id='null']")));

        mockMvc.perform(get("/healenium/healing/results").param("locator", locator).param("url", url).param("command", command)
                        .param("keyIds", keyA.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].locator.value").value(hasItem("//p[@id='a']")))
                .andExpect(jsonPath("$[*].locator.value").value(not(hasItem("//p[@id='null']"))));

        mockMvc.perform(get("/healenium/healing/results").param("locator", locator).param("url", url).param("command", command))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].locator.value").value(hasItems("//p[@id='a']", "//p[@id='null']")));

        mockMvc.perform(get("/healenium/healing").param("keyIds", "bad"))
                .andExpect(status().isBadRequest());
    }

    private void saveSelector(String uid, String className, String locator, String url, String command) {
        Selector selector = new Selector()
                .setUid(uid)
                .setClassName(className)
                .setMethodName("testMethod()")
                .setUrl(url)
                .setCommand(command)
                .setLocator(new Locator(locator, "xpath"))
                .setEnableHealing(true);
        selectorRepository.save(selector);
        selectorIds.add(uid);
    }

    private void saveHealing(UUID keyId, String selectorId, String pageContent, String resultLocator) {
        tenantTx.required(() -> {
            KeyContext.setKeyId(keyId);
            try {
                Selector selector = selectorRepository.findById(selectorId).orElseThrow();
                Healing healing = healingRepository.save(new Healing(UUID.randomUUID().toString(), selector, pageContent));
                healingIds.add(healing.getUid());
                HealingResult result = healingResultRepository.save(new HealingResult()
                        .setHealing(healing)
                        .setLocator(new Locator(resultLocator, "xpath"))
                        .setScore(0.9));
                assertEquals(keyId, healing.getKeyId());
                assertEquals(keyId, result.getKeyId());
            } finally {
                KeyContext.clear();
            }
        });
    }

    /**
     * Starts a report the way the proxies do, with the uid in the path.
     * A null header value is not sent.
     */
    private String initReport(String keyHeader, String tenantHeader) throws Exception {
        String uid = UUID.randomUUID().toString();
        MockHttpServletRequestBuilder request = post("/healenium/report/init/" + uid);
        if (keyHeader != null) {
            request.header(KeyIdFilter.KEY_HEADER, keyHeader);
        }
        if (tenantHeader != null) {
            request.header(TenantFilter.TENANT_HEADER, tenantHeader);
        }
        mockMvc.perform(request).andExpect(status().isOk());
        reportIds.add(uid);
        return uid;
    }

    private Report findReport(String uid) {
        return tenantTx.required(() -> reportRepository.findById(uid).orElseThrow());
    }
}
