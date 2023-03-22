package com.epam.healenium.service;

import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.RecordDto;

public interface ReportService {

    /**
     * Initialize new report
     *
     * @return
     */
    String initialize();

    /**
     * Initialize new report by Id
     *
     * @param uid
     * @return
     */
    String initialize(String uid);

    /**
     * Finalize report by Id
     * @param key
     * @return
     */
    RecordDto generate(String key);

    /**
     * Finalize report
     * @return
     */
    RecordDto generate();

    /**
     * Create Report Record during Save healing
     *
     * @return
     */
    void createReportRecord(HealingResult result, Healing healing, String sessionId, byte[] screenshot);

}
