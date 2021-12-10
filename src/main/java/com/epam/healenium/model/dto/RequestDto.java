package com.epam.healenium.model.dto;

import lombok.Data;

@Data
public class RequestDto {

    private String locator;
    private String className;
    private String methodName;
    private String url;

}
