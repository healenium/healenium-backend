package com.epam.healenium.restore;

import com.epam.healenium.model.dto.SessionDto;
import com.epam.healenium.node.NodeService;
import org.openqa.selenium.remote.RemoteWebDriver;

public interface RestoreDriverService {
    RemoteWebDriver restoreSession(SessionDto sessionDto);

    NodeService getNodeService();
}
