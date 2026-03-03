package com.shopfy.infrastructure.config;

import com.shopfy.infrastructure.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Instantiates an AWS SDK v2 {@link S3Client} pointed at the MinIO endpoint.
 * Path-style access is required for MinIO (virtual-hosted style is not supported
 * unless you configure custom DNS).
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public S3Client s3Client(StorageProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
                .region(Region.US_EAST_1)                // MinIO ignores region; any value works
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)    // required for MinIO
                        .build())
                .build();
    }
}
