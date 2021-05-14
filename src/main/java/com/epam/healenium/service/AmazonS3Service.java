package com.epam.healenium.service;

import com.epam.healenium.model.domain.HealingResult;


public interface AmazonS3Service {

    void uploadObject(HealingResult selectedResult, String targetLocator, String hostProject);

    void moveObject(String sourceBucketName, String destinationBucketName, HealingResult healingResult);
}
