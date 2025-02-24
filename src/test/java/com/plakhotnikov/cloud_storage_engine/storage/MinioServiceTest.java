package com.plakhotnikov.cloud_storage_engine.storage;

import com.plakhotnikov.cloud_storage_engine.exception.DownloadException;
import com.plakhotnikov.cloud_storage_engine.storage.service.MinioService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class MinioServiceTest {
    @Autowired
    private MinioService minioService;


    public static final MinIOContainer minioContainer =
            new MinIOContainer(DockerImageName.parse("minio/minio:latest"))
                    .withEnv("MINIO_ACCESS_KEY", "minioadmin")
                    .withEnv("MINIO_SECRET_KEY", "minioadmin");

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", minioContainer::getS3URL);
        registry.add("minio.accessKey", minioContainer::getUserName);
        registry.add("minio.secretKey", minioContainer::getPassword);
        registry.add("minio.bucketName", () -> "root");
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

    @Test
    void upload_file_test() {
        try {
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
            minioService.upload(mockMultipartFile.getOriginalFilename(), mockMultipartFile);
            InputStream download = minioService.download("test.txt");
            byte[] bytes = download.readAllBytes();
            assertThat(bytes).isEqualTo("hello".getBytes());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void download_file_test() {
        assertThrows(DownloadException.class, () -> minioService.download("file.txt"));
    }

    @Test
    void delete_file_test() {
        try {
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
            minioService.upload(mockMultipartFile.getOriginalFilename(), mockMultipartFile);
            InputStream download = minioService.download("test.txt");
            byte[] bytes = download.readAllBytes();
            assertThat(bytes).isEqualTo("hello".getBytes());
            minioService.deleteFile("test.txt");
            assertThrows(DownloadException.class, () -> minioService.download("test.txt"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
