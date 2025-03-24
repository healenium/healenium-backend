package com.epam.healenium.model.dto;

import com.epam.healenium.treecomparing.Node;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode
public class SelectorRequestDto {

    private String id;
    private String type;
    @ToString.Exclude
    private List<List<Node>> nodePath;
    private List<String> elementIds;
    private String sessionId;
    private boolean enableHealing;
    private boolean urlKey;
    @NotBlank
    private String locator;
    @NotBlank
    private String className;
    @NotBlank
    private String methodName;
    @Pattern(regexp = "^(findElement|findElements)$", message = "The command must be either 'findElement' or 'findElements'")
    private String command;
    @NotBlank
    private String url;
}

