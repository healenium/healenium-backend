package com.epam.healenium.model.domain;

import com.epam.healenium.model.Locator;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "healing_result_document")
public class HealingResult {

    @Id
    private String uid;
    private Locator locator;
    private Double score;
    @ToString.Exclude
    private String screenshotPath;
    private LocalDateTime createDate;

}