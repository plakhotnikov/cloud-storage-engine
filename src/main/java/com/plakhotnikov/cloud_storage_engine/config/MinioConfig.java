package com.plakhotnikov.cloud_storage_engine.config;

import com.plakhotnikov.cloud_storage_engine.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@RequiredArgsConstructor
public class MinioConfig {
    private final MinioProperties minioProperties;
    @Bean
    @SneakyThrows
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
        }
        return minioClient;
    }


}
