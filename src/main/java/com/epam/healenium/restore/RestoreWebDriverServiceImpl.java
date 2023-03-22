package com.epam.healenium.restore;

import com.epam.healenium.command.HealeniumCommandExecutor;
import com.epam.healenium.model.dto.SessionDto;
import com.epam.healenium.node.NodeService;
import com.epam.healenium.node.WebNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class RestoreWebDriverServiceImpl implements RestoreDriverService {

    private final WebNodeService webNodeService;

    @Override
    public RemoteWebDriver restoreSession(SessionDto sessionDto) {
        try {
            HttpCommandExecutor executor = new HealeniumCommandExecutor(sessionDto.getAddressOfRemoteServer(), sessionDto.getSessionId(), sessionDto.getSessionCapabilities());
            return new RemoteWebDriver(executor, new MutableCapabilities(sessionDto.getSessionCapabilities()));
        } catch (Exception ex) {
            log.warn("[Restore Session] Error during restore. Message: {}, Ex: {}", ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public NodeService getNodeService() {
        return webNodeService;
    }


}
