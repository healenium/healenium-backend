package com.epam.healenium.model.wrapper;

import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Selector;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class RecordWrapper {

    private Set<Record> records = new HashSet<>();

    /**
     * Add record to report
     * @param healing
     * @param healingResult
     */
    public void addRecord(Healing healing, HealingResult healingResult, String screenshotPath){
        Selector selector = healing.getSelector();

        Record record = new Record();
        record.setName(selector.getName());
        record.setClassName(selector.getClassName());
        record.setMethodName(selector.getMethodName());
        record.setFailedLocator(selector.getLocator());
        record.setHealedLocator(healingResult.getLocator());
        record.setScreenShotPath(screenshotPath);
        record.setHealingResultId(healingResult.getId());
        records.add(record);
    }

    @Data
    public static class Record implements Serializable {
        private String name;
        private String className;
        private String methodName;
        private Locator failedLocator;
        private Locator healedLocator;
        @ToString.Exclude
        private String screenShotPath;
        private Integer healingResultId;
    }
}
