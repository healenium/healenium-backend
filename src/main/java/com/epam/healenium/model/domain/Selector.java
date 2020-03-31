package com.epam.healenium.model.domain;

import com.epam.healenium.model.Locator;
import com.epam.healenium.treecomparing.Node;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contains selector information.
 * Speaking general, this is locator used if specific test class method and it's last successful position on page .
 */

@Accessors(chain = true)
@Data
@Document(collection = "selector_document")
public class Selector {

    @Id
    private String uid;
    private String name;
    private String className;
    private String methodName;
    private Locator locator;
    @ToString.Exclude
    private List<Node> nodePath;
    private LocalDateTime createdDate;

}