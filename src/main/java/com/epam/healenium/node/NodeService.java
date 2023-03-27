package com.epam.healenium.node;

import com.epam.healenium.treecomparing.Node;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public interface NodeService {
    List<Node> getNodePath(WebDriver driver, WebElement element);

    String getCurrentUrl(WebDriver driver);
}
