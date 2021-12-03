package com.epam.healenium.service;

import com.epam.healenium.model.dto.RecordDto;

public interface ReportService {

    /**
     * Initialize new report
     * @return
     */
    String initialize();

    /**
     * Initialize new report
     * @param uid
     * @return
     */
    String initialize(String uid);

    /**
     * Finalize report
     * @param key
     * @return
     */
    RecordDto generate(String key);


}
