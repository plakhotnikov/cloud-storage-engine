package com.plakhotnikov.cloud_storage_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CloudStorageEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudStorageEngineApplication.class, args);
    }
}
