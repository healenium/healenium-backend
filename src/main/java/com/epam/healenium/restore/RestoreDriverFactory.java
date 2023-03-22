package com.epam.healenium.restore;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.CapabilityType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j(topic = "healenium")
@Service
public class RestoreDriverFactory {

    private final RestoreDriverService restoreWebDriverService;
    private final RestoreDriverService restoreMobileNativeDriverService;

    public RestoreDriverFactory(@Qualifier("restoreWebDriverServiceImpl") RestoreDriverService restoreWebDriverService,
                                @Qualifier("restoreMobileNativeDriverServiceImpl") RestoreDriverService restoreMobileNativeDriverService) {
        this.restoreWebDriverService = restoreWebDriverService;
        this.restoreMobileNativeDriverService = restoreMobileNativeDriverService;
    }

    public RestoreDriverService getRestoreService(Map<String, Object> capabilities) {
        return capabilities.containsKey(CapabilityType.BROWSER_NAME)
                ? restoreWebDriverService
                : restoreMobileNativeDriverService;
    }
}
