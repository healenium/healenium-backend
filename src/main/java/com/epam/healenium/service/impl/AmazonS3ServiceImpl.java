package com.epam.healenium.service.impl;

import com.amazonaws.services.s3.model.ObjectListing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.repository.AmazonS3Repository;
import com.epam.healenium.service.AmazonS3Service;
import com.epam.healenium.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;

import static com.epam.healenium.constants.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {

    private final AmazonS3Repository amazonS3Repository;

    @Override
    public void uploadObject(String metrics, HealingResult selectedResult, String hostProject) {
        String fileName = getFileName(selectedResult).toString();
        try {
            File file = new File(fileName);
            new BufferedWriter(new FileWriter(fileName))
                    .append(metrics)
                    .close();
            amazonS3Repository.putObject(SUCCESSFUL_HEALING_BUCKET, getObjectKey(fileName, hostProject), file);
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void moveObject(String sourceBucketName, String destinationBucketName, HealingResult healingResult) {
        StringBuilder fileName = getFileName(healingResult);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY);
        String createDate = healingResult.getCreateDate().format(formatter);

        ObjectListing objectListing = amazonS3Repository.listObjects(sourceBucketName, createDate);
        objectListing.getObjectSummaries().stream()
                .filter(object -> object.getKey().contains(fileName))
                .findFirst()
                .ifPresent(object -> amazonS3Repository.moveObject(sourceBucketName,
                        destinationBucketName, object.getKey()));
    }

    private String getObjectKey(String fileName, String hostProject) {
        return new StringBuilder()
                .append(Utils.getCurrentDate(DD_MMM_YYYY)).append("/")
                .append(hostProject).append("/")
                .append(fileName)
                .toString();
    }

    private StringBuilder getFileName(HealingResult selectedResult) {
        String selectedResultId = selectedResult.getId().toString();
        String healingUid = selectedResult.getHealing().getUid();
        return new StringBuilder()
                .append(healingUid)
                .append("_")
                .append(selectedResultId)
                .append(TXT);
    }

}
