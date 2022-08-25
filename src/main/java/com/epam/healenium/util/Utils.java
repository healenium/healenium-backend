package com.epam.healenium.util;

import jdk.internal.joptsimple.internal.Strings;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@UtilityClass
public class Utils {

    /**
     * Builds ID for element that represent selector meta
     *
     * @param className the fully qualified name of the class
     * @param locator   the selector value
     * @param url
     * @return hashed key of locator
     */
    public String buildKey(String className, String locator, String url) {
        String rawKey = className.concat(url) + locator.hashCode();
        return DigestUtils.md5DigestAsHex(rawKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    public String buildKey(String className, String locator, String url, boolean urlForKey, boolean pathForKey) {
        String addressForKey = getAddressForKey(url, urlForKey, pathForKey);
        return buildKey(className, locator, addressForKey);
    }

    /**
     * Builds ID for healing record
     *
     * @param elementId  unique identifier of selector META
     * @param pageSource searching context object
     * @return hashed key of healing element
     */
    public String buildHealingKey(String elementId, String pageSource) {
        String rawKey = elementId + pageSource.hashCode();
        return DigestUtils.md5DigestAsHex(rawKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds Screenshot name
     *
     * @return name
     */
    public String buildScreenshotName() {
        return "screenshot_" + LocalDateTime.now().format(DateTimeFormatter
                .ofPattern("dd-MMM-yyyy-hh-mm-ss.SSS").withLocale(Locale.US)) + ".png";
    }

    public String getAddressForKey(String url, boolean urlForKey, boolean pathForKey) {
        try {
            if (urlForKey && pathForKey) {
                return url;
            }
            if (urlForKey && !pathForKey) {
                return url.substring(0, url.indexOf("/") + 1);
            }
            if (!urlForKey && pathForKey) {
                return url.substring(url.indexOf("/"));
            }
            if (!urlForKey && !pathForKey) {
                return Strings.EMPTY;
            }
        } catch (Exception e) {
            log.warn("Error during parse url. Message: {}", e.getMessage());
        }
        return Strings.EMPTY;
    }
}