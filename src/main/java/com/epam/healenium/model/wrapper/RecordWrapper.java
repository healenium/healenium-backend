package com.epam.healenium.model.wrapper;

import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Selector;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class RecordWrapper {

    private Set<Record> records = new HashSet<>();

    /**
     * Add record to report
     *
     * @param healing
     * @param healingResult
     */
    public void addRecord(Healing healing, HealingResult healingResult, String screenshotPath) {
        Selector selector = healing.getSelector();

        records.add(new Record()
                .setClassName(selector.getClassName())
                .setMethodName(selector.getMethodName())
                .setFailedLocator(selector.getLocator())
                .setHealedLocator(healingResult.getLocator())
                .setScreenShotPath(screenshotPath)
                .setHealingResultId(healingResult.getId()));
    }

    @Data
    @Accessors(chain = true)
    public static class Record implements Serializable {
        private String className;
        private String methodName;
        private Locator failedLocator;
        private Locator healedLocator;
        @ToString.Exclude
        private String screenShotPath;
        private Integer healingResultId;
    }
}
