package com.plakhotnikov.cloud_storage_engine.storage;

import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.FileEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.StorageMapper;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.FileDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.SubDirectoryDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class StorageMapperTest {
    @Autowired
    private StorageMapper storageMapper;


    public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("password");

    @BeforeAll
    static void startContainer() {
        postgresContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @AfterAll
    static void stopContainer() {
        postgresContainer.stop();
    }


    @Test
    void file_to_dto_test() {
        FileEntity fileEntity = new FileEntity();

        UUID uuid = UUID.randomUUID();
        fileEntity.setId(uuid);
        fileEntity.setCheckSum("123432");
        fileEntity.setExtension("txt");
        fileEntity.setFilename("filename");

        FileDto fileDto = storageMapper.fileToDto(fileEntity);
        assertAll(
                () -> assertThat(fileDto.getId()).isEqualTo(fileEntity.getId()),
                () -> assertThat(fileDto.getExtension()).isEqualTo(fileEntity.getExtension()),
                () -> assertThat(fileDto.getFilename()).isEqualTo(fileEntity.getFilename())
        );
    }

    @Test
    void directory_to_sub_dir_dto_test() {
        DirectoryEntity directoryEntity = new DirectoryEntity();
        directoryEntity.setName("test_directory");
        directoryEntity.setId(1L);
        SubDirectoryDto subDirectoryDto = storageMapper.dirToSubDto(directoryEntity);

        assertAll(
                () -> assertThat(subDirectoryDto.getId()).isEqualTo(directoryEntity.getId()),
                () -> assertThat(subDirectoryDto.getName()).isEqualTo(directoryEntity.getName())
        );
    }


    @Test
    void directory_to_dto_test() {
        DirectoryEntity directoryEntity = new DirectoryEntity();
        directoryEntity.setName("test_directory");
        directoryEntity.setId(1L);
        DirectoryEntity child1 = new DirectoryEntity();
        child1.setName("child1");
        child1.setId(2L);
        DirectoryEntity child2 = new DirectoryEntity();
        child2.setName("child2");
        child2.setId(3L);
        directoryEntity.setRootDirectory(null);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(UUID.randomUUID());
        fileEntity.setCheckSum("123432");
        fileEntity.setExtension("txt");
        fileEntity.setFilename("filename");
        directoryEntity.setChildren(List.of(child1, child2));
        directoryEntity.setFiles(List.of(fileEntity));
        child2.setFiles(List.of(fileEntity));

        DirectoryDto directoryDto = storageMapper.dirToDto(directoryEntity);
        var files = directoryDto.getFiles();
        var subDirs = directoryDto.getChildren();

        assertAll(
                () -> assertThat(directoryDto.getId()).isEqualTo(directoryEntity.getId()),
                () -> assertThat(directoryDto.getName()).isEqualTo(directoryEntity.getName()),
                () -> assertThat(files.size()).isEqualTo(1),
                () -> assertThat(files.getFirst().getId()).isEqualTo(fileEntity.getId()),
                () -> assertThat(files.getFirst().getFilename()).isEqualTo(fileEntity.getFilename()),
                () -> assertThat(files.getFirst().getExtension()).isEqualTo(fileEntity.getExtension()),
                () -> assertThat(subDirs.size()).isEqualTo(2),
                () -> assertThat(subDirs.getFirst().getId()).isEqualTo(child1.getId()),
                () -> assertThat(subDirs.getFirst().getName()).isEqualTo(child1.getName()),
                () -> assertThat(subDirs.get(1).getId()).isEqualTo(child2.getId()),
                () -> assertThat(subDirs.get(1).getName()).isEqualTo(child2.getName())
        );

    }



}
