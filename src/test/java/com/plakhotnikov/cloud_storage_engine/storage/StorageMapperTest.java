package com.plakhotnikov.cloud_storage_engine.storage;

import com.plakhotnikov.cloud_storage_engine.storage.entity.Directory;
import com.plakhotnikov.cloud_storage_engine.storage.entity.File;
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
        File file = new File();

        UUID uuid = UUID.randomUUID();
        file.setId(uuid);
        file.setCheckSum("123432");
        file.setExtension("txt");
        file.setFilename("filename");

        FileDto fileDto = storageMapper.fileToDto(file);
        assertAll(
                () -> assertThat(fileDto.getId()).isEqualTo(file.getId()),
                () -> assertThat(fileDto.getExtension()).isEqualTo(file.getExtension()),
                () -> assertThat(fileDto.getFilename()).isEqualTo(file.getFilename())
        );
    }

    @Test
    void directory_to_sub_dir_dto_test() {
        Directory directory = new Directory();
        directory.setName("test_directory");
        directory.setId(1L);
        SubDirectoryDto subDirectoryDto = storageMapper.dirToSubDto(directory);

        assertAll(
                () -> assertThat(subDirectoryDto.getId()).isEqualTo(directory.getId()),
                () -> assertThat(subDirectoryDto.getName()).isEqualTo(directory.getName())
        );
    }


    @Test
    void directory_to_dto_test() {
        Directory directory = new Directory();
        directory.setName("test_directory");
        directory.setId(1L);
        Directory child1 = new Directory();
        child1.setName("child1");
        child1.setId(2L);
        Directory child2 = new Directory();
        child2.setName("child2");
        child2.setId(3L);
        directory.setRootDirectory(null);
        File file = new File();
        file.setId(UUID.randomUUID());
        file.setCheckSum("123432");
        file.setExtension("txt");
        file.setFilename("filename");
        directory.setChildren(List.of(child1, child2));
        directory.setFiles(List.of(file));
        child2.setFiles(List.of(file));

        DirectoryDto directoryDto = storageMapper.dirToDto(directory);
        var files = directoryDto.getFiles();
        var subDirs = directoryDto.getChildren();

        assertAll(
                () -> assertThat(directoryDto.getId()).isEqualTo(directory.getId()),
                () -> assertThat(directoryDto.getName()).isEqualTo(directory.getName()),
                () -> assertThat(files.size()).isEqualTo(1),
                () -> assertThat(files.getFirst().getId()).isEqualTo(file.getId()),
                () -> assertThat(files.getFirst().getFilename()).isEqualTo(file.getFilename()),
                () -> assertThat(files.getFirst().getExtension()).isEqualTo(file.getExtension()),
                () -> assertThat(subDirs.size()).isEqualTo(2),
                () -> assertThat(subDirs.getFirst().getId()).isEqualTo(child1.getId()),
                () -> assertThat(subDirs.getFirst().getName()).isEqualTo(child1.getName()),
                () -> assertThat(subDirs.get(1).getId()).isEqualTo(child2.getId()),
                () -> assertThat(subDirs.get(1).getName()).isEqualTo(child2.getName())
        );

    }



}
