package com.plakhotnikov.cloud_storage_engine.storage;


import com.plakhotnikov.cloud_storage_engine.storage.controller.StorageController;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.CreateDirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.FileDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.MoveFileDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class StorageControllerTest {
    @Autowired
    private StorageController storageController;


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
    public void create_dir_test() {
        var createDto = new CreateDirectoryDto(null, "test-create");
        var res = storageController.createDirectory(createDto);
        assertAll(
                () -> assertThat(res.getId()).isNotNull(),
                () -> assertThat(res.getName()).isEqualTo(createDto.getName())
        );
    }

    @Test
    public void list_test() {
        var createDto = new CreateDirectoryDto(null, "list-test");
        var dir = storageController.createDirectory(createDto);
        var createDto2 = new CreateDirectoryDto(dir.getId(), "list-test1");
        storageController.createDirectory(createDto2);
        DirectoryDto list = storageController.list(dir.getId());
        assertAll(
                () -> assertThat(list.getName()).isEqualTo(createDto.getName()),
                () -> assertThat(list.getChildren().getFirst().getName()).isEqualTo(createDto2.getName())
        );
    }

    @Test
    public void upload_test() {
        var createDto = new CreateDirectoryDto(null, "upload-test");
        var dir = storageController.createDirectory(createDto);
        MockMultipartFile file = new MockMultipartFile("test", "test.txt", "text/plain", "hello".getBytes());
        FileDto saved = storageController.uploadFile(dir.getId(), file);
        ResponseEntity<byte[]> response = storageController.downloadFile(saved.getId());
        assertThat(response.getBody()).isEqualTo("hello".getBytes());
    }

    @Test
    public void move_file_test() {
        var createDto = new CreateDirectoryDto(null, "moveFileToDir-test");
        var dir = storageController.createDirectory(createDto);
        var createDto2 = new CreateDirectoryDto(dir.getId(), "moveFileToDir-test1");
        var dir2 = storageController.createDirectory(createDto2);
        MockMultipartFile file = new MockMultipartFile("test", "test.txt", "text/plain", "hello".getBytes());
        FileDto saved = storageController.uploadFile(dir.getId(), file);
        storageController.moveFile(new MoveFileDto(saved.getId(), dir2.getId()));
        var root = storageController.list(dir.getId());
        var target = storageController.list(dir2.getId());

        assertAll(
                () -> assertThat(root.getFiles().size()).isEqualTo(0),
                () -> assertThat(target.getFiles().size()).isEqualTo(1)
        );
    }
}
