package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VcsDto {
    private String name;
    private String accessToken;
    private String repository;
    private String branch;
}

