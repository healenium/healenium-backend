package com.epam.healenium.service.selector;

import com.epam.healenium.tenant.TenantContext;
import com.epam.healenium.tenant.TenantFilter;
import com.epam.healenium.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Universal selector ID generator scoped to tenant.
 * Uses {@link TenantFilter#DEFAULT_TENANT_ID} when tenant context is absent.
 */
@Slf4j
@Component
public class DefaultSelectorIdStrategy implements SelectorIdStrategy {

    @Override
    public String getSelectorId(String locator, String url, String command, boolean urlForKey) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = TenantFilter.DEFAULT_TENANT_ID;
        }

        String addressForKey = Utils.getAddressForKey(url, urlForKey);
        String id = Utils.buildKey(locator, command, addressForKey, tenantId.toString());

        log.debug("[Selector ID] Tenant: {}, Locator: {}, URL(source): {}, URL(key): {}, Command: {}, KEY_SELECTOR_URL: {}",
                tenantId, locator, url, addressForKey, command, urlForKey);
        log.debug("[Selector ID] Result ID: {}", id);
        return id;
    }
}
