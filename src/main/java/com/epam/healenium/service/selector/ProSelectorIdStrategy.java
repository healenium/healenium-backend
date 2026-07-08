package com.epam.healenium.service.selector;

import com.epam.healenium.tenant.TenantContext;
import com.epam.healenium.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile("pro")
public class ProSelectorIdStrategy implements SelectorIdStrategy {

    @Override
    public String getSelectorId(String locator, String url, String command, boolean urlForKey) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant is not set in TenantContext");
        }

        String addressForKey = Utils.getAddressForKey(url, urlForKey);
        String id = Utils.buildKey(locator, command, addressForKey, tenantId.toString());

        log.debug("[Selector ID] (PRO) Tenant: {}, Locator: {}, URL(source): {}, URL(key): {}, Command: {}, KEY_SELECTOR_URL: {}",
                tenantId, locator, url, addressForKey, command, urlForKey);
        log.debug("[Selector ID] (PRO) Result ID: {}", id);
        return id;
    }
}
