package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReportDto {
    private String id;
    private String name;
    private String date;
}
