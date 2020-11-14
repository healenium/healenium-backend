package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RecordDto {

    private String name = "Healing Report";
    private String time;
    private List<ReportRecord> data = new ArrayList<>();

    @Data
    public static class ReportRecord {
        private String declaringClass;
        private String screenShotPath;
        private String failedLocatorValue;
        private String failedLocatorType;
        private String healedLocatorValue;
        private String healedLocatorType;
        private boolean successHealing;
        private Integer healingResultId;
    }
}
