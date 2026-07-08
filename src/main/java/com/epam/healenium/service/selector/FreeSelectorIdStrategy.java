package com.epam.healenium.service.selector;

import com.epam.healenium.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("free")
public class FreeSelectorIdStrategy implements SelectorIdStrategy {

    @Override
    public String getSelectorId(String locator, String url, String command, boolean urlForKey) {
        String addressForKey = Utils.getAddressForKey(url, urlForKey);
        String id = Utils.buildKey(locator, command, addressForKey);
        log.debug("[Selector ID] Locator: {}, URL(source): {}, URL(key): {}, Command: {}, KEY_SELECTOR_URL: {}",
                locator, url, addressForKey, command, urlForKey);
        log.debug("[Selector ID] Result ID: {}", id);
        return id;
    }
}
