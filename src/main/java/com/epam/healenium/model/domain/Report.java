package com.epam.healenium.model.domain;

import com.epam.healenium.model.Locator;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent report about healing during test.
 * Contains information about healed locators and value that was selected as healed.
 */

@Accessors(chain = true)
@Data
@Document(collection = "report_document")
public class Report {

    @Id
    private String uid;
    private Set<Record> elements = new HashSet<>();
    private LocalDateTime createdDate;

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
        elements.add(record);
    }

    @Data
    public static class Record {
        private String name;
        private String className;
        private String methodName;
        private Locator failedLocator;
        private Locator healedLocator;
        @ToString.Exclude
        private String screenShotPath;
    }
}
