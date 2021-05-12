package com.epam.healenium.repository;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
public class AmazonS3Repository {

    private final AmazonS3Client s3Client;

    public AmazonS3Repository(AmazonS3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void deleteObject(String bucket, String key) {
        s3Client.deleteObject(bucket, key);
    }

    public void putObject(String bucket, String key, File file) {
        s3Client.putObject(bucket, key, file);
    }

    public ObjectListing listObjects(String bucketName, String prefix) {
        return s3Client.listObjects(bucketName, prefix);
    }

    public void moveObject(String sourceBucketName, String destinationBucketName, String key) {
        s3Client.copyObject(sourceBucketName, key, destinationBucketName, key);
        deleteObject(sourceBucketName, key);
    }
}
