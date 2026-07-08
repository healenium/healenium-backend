package com.epam.healenium.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {

    private String locator;
    private String className;
    private String methodName;
    private String command;
    private String url;
    @ToString.Exclude
    private String pageContent;

}
