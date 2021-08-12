package com.epam.healenium.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.epam.healenium.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AmazonS3Configuration {

    @Bean
    public AmazonS3 amazonS3Client() {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(
                    Utils.getKeyFromKeyStore("s3accessKey", "LrnvyUgeQWddkjDw3se", "keystore/s3metrics.ks"),
                    Utils.getKeyFromKeyStore("s3secretKey", "LrnvyUgeQWddkjDw3se", "keystore/s3metrics.ks"));
            return AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build();
        } catch (Exception ex) {
            log.warn("Error during create AmazonS3: {}", ex.getMessage());
            return new AmazonS3Client();
        }
    }
}
