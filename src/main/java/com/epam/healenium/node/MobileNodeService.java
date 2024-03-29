package com.epam.healenium.node;

import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.NodeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "healenium")
@Service
public class MobileNodeService implements NodeService {

    private static final int ONE_ELEMENT = 1;
    private static final int THE_ONLY_ELEMENT = 0;

    @Override
    public List<Node> getNodePath(WebDriver driver, WebElement element) {
        String xmlString = driver.getPageSource();
        Document doc = Jsoup.parse(xmlString, "", Parser.xmlParser());
        Element currentElementInDoc = getElementFromDoc(doc, element);
        List<Node> list = new ArrayList<>();

        while (currentElementInDoc.hasParent()) {
            Node currentNode = toNode(currentElementInDoc);
            list.add(currentNode);
            currentElementInDoc = currentElementInDoc.parent();
        }
        Collections.reverse(list);
        return new LinkedList<>(list);
    }

    private Element getElementFromDoc(Document doc, WebElement webElement) {
        List<String> paramsList = Arrays.asList("resource-id", "content-desc", "text", "class", "bounds", "checked",
                "enabled", "selected", "focused", "displayed");

        List<Element> tempElements = new ArrayList<>(doc.getAllElements());
        Iterator<String> it = paramsList.iterator();

        if (tempElements.size() == ONE_ELEMENT) {
            return tempElements.get(THE_ONLY_ELEMENT);
        }

        while (it.hasNext()) {
            String nextParam = it.next();
            String tempValue = webElementParamValue(nextParam, webElement);
            tempElements.removeIf(e -> !e.attributes().get(nextParam).equals(tempValue));

            if (tempElements.size() == ONE_ELEMENT) {
                return tempElements.get(THE_ONLY_ELEMENT);
            }
        }
        return tempElements.get(THE_ONLY_ELEMENT);
    }

    private String webElementParamValue(String currentAttribute, WebElement webElement) {
        String temp = webElement.getAttribute(currentAttribute);
        return temp != null ? temp : "";
    }

    private Node toNode(Element e) {
        Map<String, String> otherAttributes = new HashMap<>();
        List<Attribute> list = e.attributes().asList();
        list.forEach(attr -> otherAttributes.put(attr.getKey(), attr.getValue()));

        return new NodeBuilder()
                .setTag(e.attributes().get("class"))
                .setContent(Collections.singletonList(e.attributes().get("text")))
                .setOtherAttributes(otherAttributes)
                .build();
    }

    public String getCurrentUrl(WebDriver driver) {
        return (String) ((RemoteWebDriver) driver).getCapabilities().asMap()
                .getOrDefault("appium:appActivity", "");
    }
}
