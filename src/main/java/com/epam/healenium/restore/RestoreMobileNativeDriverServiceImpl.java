package com.epam.healenium.restore;

import com.epam.healenium.command.HealeniumMobileCommandExecutor;
import com.epam.healenium.model.dto.SessionDto;
import com.epam.healenium.node.MobileNodeService;
import com.epam.healenium.node.NodeService;
import io.appium.java_client.AppiumDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class RestoreMobileNativeDriverServiceImpl implements RestoreDriverService {

    private final MobileNodeService mobileNodeService;

    @Override
    public RemoteWebDriver restoreSession(SessionDto sessionDto) {
        try {
            HttpCommandExecutor executor = new HealeniumMobileCommandExecutor(sessionDto.getAddressOfRemoteServer(), sessionDto.getSessionId(), sessionDto.getSessionCapabilities());
            return new AppiumDriver(executor, new DesiredCapabilities(sessionDto.getSessionCapabilities()));
        } catch (Exception ex) {
            log.warn("[Restore Session] Error during restore. Message: {}, Ex: {}", ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public NodeService getNodeService() {
        return mobileNodeService;
    }

}
