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
    private String locatorType;
    private String className;
    private String methodName;
    private String command;
    private String url;
    @ToString.Exclude
    private String pageContent;
    /** Per-run overrides. A missing or invalid value falls back to the tenant settings. */
    private String healedType;
    private Double scoreCap;
    private Integer recoveryTries;

}
