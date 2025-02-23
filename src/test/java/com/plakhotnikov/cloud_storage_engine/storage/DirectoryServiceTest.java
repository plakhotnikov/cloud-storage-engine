package com.plakhotnikov.cloud_storage_engine.storage;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.CreateDirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.service.DirectoryService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DirectoryServiceTest {
    @Autowired
    private DirectoryService directoryService;




    @BeforeEach
    public void setAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("abc@abc.ru", "123"));
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
    void create_directory_test() {
        CreateDirectoryDto createDirectoryDto = new CreateDirectoryDto(null, "test_dir");

        DirectoryDto dto = directoryService.createDirectory(createDirectoryDto);
        assertAll(
                () -> assertThat(dto.getId()).isNotNull(),
                () -> assertThat(dto.getName()).isEqualTo(createDirectoryDto.getName()),
                () -> assertThat(dto.getFiles()).isEqualTo(List.of()),
                () -> assertThat(dto.getChildren()).isEqualTo(List.of())
        );
    }

    @Test
    void is_user_owner_test() {
        CreateDirectoryDto createDirectoryDto = new CreateDirectoryDto(null, "test_is_user_owner");

        DirectoryDto dto = directoryService.createDirectory(createDirectoryDto);
        assertThat(directoryService.isUserOwner(dto.getId())).isTrue();
    }


    @Test
    void get_directory_test() {
        CreateDirectoryDto createDirectoryDto = new CreateDirectoryDto(null, "root1");
        DirectoryDto rootDir = directoryService.createDirectory(createDirectoryDto);
        CreateDirectoryDto createDirectoryDto2 = new CreateDirectoryDto(rootDir.getId(), "dir1");
        CreateDirectoryDto createDirectoryDto3 = new CreateDirectoryDto(rootDir.getId(), "dir2");
        DirectoryDto directory1 = directoryService.createDirectory(createDirectoryDto2);
        DirectoryDto directory2 = directoryService.createDirectory(createDirectoryDto3);

        DirectoryDto dto2 = directoryService.getDirectory(rootDir.getId());

        assertAll(
                () -> assertThat(dto2.getId()).isNotNull(),
                () -> assertThat(dto2.getName()).isEqualTo(createDirectoryDto.getName()),
                () -> assertAll(
                        () -> assertThat(dto2.getChildren().size()).isEqualTo(2),
                        () -> assertThat(dto2.getChildren().getFirst().getName()).isEqualTo(createDirectoryDto2.getName()),
                        () -> assertThat(dto2.getChildren().get(1).getName()).isEqualTo(createDirectoryDto3.getName())
                ),
                () -> assertThat(dto2.getFiles()).isEqualTo(List.of())
        );
    }
}
