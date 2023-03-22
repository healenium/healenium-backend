package com.epam.healenium.model;

import com.epam.healenium.node.NodeService;
import lombok.Data;
import lombok.experimental.Accessors;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@Accessors(chain = true)
public class SessionContext {

    private RemoteWebDriver remoteWebDriver;
    private NodeService nodeService;
}
