package com.epam.healenium.tenant;

import com.epam.healenium.initializer.TestContainersInitializer;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.tenant.registry.Tenant;
import com.epam.healenium.tenant.registry.TenantRepository;
import com.epam.healenium.tenant.registry.TenantStatuses;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("pro")
class MultitenancyRlsIntegrationTest extends TestContainersInitializer {

    private static final UUID TENANT_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TENANT_B = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @DynamicPropertySource
    static void proLiquibase(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/changelog-pro.xml");
        // Repository/IT path does not hit HTTP; set a token so any accidental web calls are not open.
        registry.add("healenium.m2m.internal-token", () -> "test-internal-token");
    }

    @Autowired
    private SelectorRepository selectorRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TenantTxFacade tenantTx;

    @Autowired
    private TenantValidationService tenantValidationService;

    @BeforeEach
    void setUp() {
        ensureTenant(TENANT_A, "Tenant A");
        ensureTenant(TENANT_B, "Tenant B");
        tenantValidationService.invalidate(TENANT_A);
        tenantValidationService.invalidate(TENANT_B);
        deleteAllForTenant(TENANT_A);
        deleteAllForTenant(TENANT_B);
    }

    @AfterEach
    void tearDown() {
        deleteAllForTenant(TENANT_A);
        deleteAllForTenant(TENANT_B);
    }

    @Test
    void readIsolationBetweenTenants() {
        saveSelector(TENANT_A, "selector-a");
        saveSelector(TENANT_B, "selector-b");

        List<Selector> tenantARows = findAll(TENANT_A);
        List<Selector> tenantBRows = findAll(TENANT_B);

        assertThat(tenantARows).hasSize(1);
        assertThat(tenantBRows).hasSize(1);
        assertThat(tenantARows.get(0).getUid()).isEqualTo("selector-a");
        assertThat(tenantBRows.get(0).getUid()).isEqualTo("selector-b");
    }

    @Test
    void findAllReturnsOnlyCurrentTenantRows() {
        saveSelector(TENANT_A, "only-a1");
        saveSelector(TENANT_A, "only-a2");
        saveSelector(TENANT_B, "only-b1");

        assertThat(findAll(TENANT_A)).hasSize(2);
        assertThat(findAll(TENANT_B)).hasSize(1);
    }

    @Test
    void repositoryWithoutSetLocalSeesNoRows() {
        saveSelector(TENANT_A, "hidden");

        assertThat(selectorRepository.findAll()).isEmpty();
    }

    @Test
    void localValidationAcceptsActiveTenant() {
        assertThat(tenantValidationService.isTenantAllowed(TENANT_A)).isTrue();
    }

    @Test
    void localValidationRejectsUnknownTenant() {
        UUID unknown = UUID.fromString("99999999-9999-9999-9999-999999999999");
        assertThat(tenantValidationService.isTenantAllowed(unknown)).isFalse();
    }

    private void ensureTenant(UUID id, String name) {
        Tenant tenant = tenantRepository.findById(id).orElseGet(Tenant::new);
        tenant.setId(id);
        tenant.setName(name);
        tenant.setStatus(TenantStatuses.ACTIVE);
        tenantRepository.save(tenant);
    }

    private void saveSelector(UUID tenantId, String uid) {
        withTenant(tenantId, () -> {
            Selector selector = new Selector()
                    .setUid(uid)
                    .setUrl("https://example.test")
                    .setClassName("com.example.Test")
                    .setMethodName("test()")
                    .setLocator(new Locator("#id", "css"));
            selectorRepository.saveAndFlush(selector);
        });
    }

    private List<Selector> findAll(UUID tenantId) {
        return withTenant(tenantId, () -> selectorRepository.findAll());
    }

    private void deleteAllForTenant(UUID tenantId) {
        withTenant(tenantId, () -> selectorRepository.deleteAll());
    }

    private void withTenant(UUID tenantId, Runnable action) {
        TenantContext.setTenantId(tenantId);
        try {
            tenantTx.required(action);
        } finally {
            TenantContext.clear();
        }
    }

    private <T> T withTenant(UUID tenantId, java.util.function.Supplier<T> supplier) {
        TenantContext.setTenantId(tenantId);
        try {
            return tenantTx.required(supplier);
        } finally {
            TenantContext.clear();
        }
    }
}
