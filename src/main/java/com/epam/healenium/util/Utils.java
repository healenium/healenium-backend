package com.epam.healenium.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j(topic = "healenium")
@UtilityClass
public class Utils {

    /**
     * Builds ID for element that represent selector meta
     *
     * @param locator the selector value
     * @param url
     * @return hashed key of locator
     */
    public String buildKey(String locator, String command, String url) {
        String rawKey = new StringBuilder(url)
                .append(command)
                .append(locator.hashCode())
                .toString();
        return DigestUtils.md5DigestAsHex(rawKey.trim().getBytes(StandardCharsets.UTF_8));
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

    public String getAddressForKey(String url, boolean urlForKey) {
        try {
            return urlForKey ? url : StringUtils.EMPTY;
        } catch (Exception e) {
            log.warn("Error during parse url. Message: {}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }

}