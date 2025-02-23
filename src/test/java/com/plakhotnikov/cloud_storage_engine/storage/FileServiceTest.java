package com.plakhotnikov.cloud_storage_engine.storage;

import com.plakhotnikov.cloud_storage_engine.exception.DeleteFileException;
import com.plakhotnikov.cloud_storage_engine.exception.DownloadException;
import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.CreateDirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.FileDto;
import com.plakhotnikov.cloud_storage_engine.storage.service.DirectoryService;
import com.plakhotnikov.cloud_storage_engine.storage.service.FileService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FileServiceTest {
    @Autowired
    private FileService fileService;

    @Autowired
    private DirectoryService directoryService;

    @BeforeEach
    public void setAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("abc@abc.ru", "123"));
    }

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

    public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("password");

    @BeforeAll
    static void startPostgresContainer() {
        postgresContainer.start();
    }

    @DynamicPropertySource
    static void configurePostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @AfterAll
    static void stopPostgresContainer() {
        postgresContainer.stop();
    }

    @Test
    void download_not_found_file_test() {
        assertThrows(
                DownloadException.class,
                () -> fileService.downloadFile(UUID.randomUUID()));
    }

    @Test
    void upload_file_test() {
        try {
            DirectoryDto directoryDto = directoryService.createDirectory(
                    new CreateDirectoryDto(null, "test")
            );
            MockMultipartFile file = new MockMultipartFile("test", "test.txt", "blank/text", "hello".getBytes());
            FileDto fileDto = fileService.upload(file, directoryDto.getId());
            InputStream x = fileService.downloadFile(fileDto.getId());
            byte[] bytes = x.readAllBytes();
            assertThat(bytes).isEqualTo("hello".getBytes());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void delete_file_test() {
        DirectoryDto directoryDto = directoryService.createDirectory(
                new CreateDirectoryDto(null, "test")
        );
        MockMultipartFile file = new MockMultipartFile("test", "test.txt", "blank/text", "hello".getBytes());
        FileDto fileDto = fileService.upload(file, directoryDto.getId());
        fileService.deleteFile(fileDto.getId());
        assertThrows(ResourceNotFoundException.class, () -> fileService.deleteFile(fileDto.getId()));
        assertThrows(DownloadException.class, () -> fileService.downloadFile(fileDto.getId()));
    }
}
