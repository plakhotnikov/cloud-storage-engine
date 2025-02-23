package com.plakhotnikov.cloud_storage_engine.config;

import com.plakhotnikov.cloud_storage_engine.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@RequiredArgsConstructor
public class MinioConfig {
    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }


}
