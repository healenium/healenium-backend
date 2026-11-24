package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DedicatedInfo {
    private boolean hasActiveGit;
    private boolean hasActiveGenAI;
    private boolean hasRecords;
    private boolean noHealingResult;
}
