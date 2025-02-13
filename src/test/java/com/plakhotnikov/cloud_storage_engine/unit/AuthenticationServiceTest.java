package com.plakhotnikov.cloud_storage_engine.unit;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.services.AuthenticationService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthenticationServiceTest {
    @Autowired
    private AuthenticationService authenticationService;

    public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest")
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
    void loginTest() {
        UserResponseDto user = authenticationService.login(new UserLoginDto(
                "abc@aBc.ru", "123"
        ));
        assertNotNull(user.getRefreshToken());
        assertFalse(user.getRefreshToken().isEmpty());
    }

    @Test
    void failureLoginWrongPassword() {
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(new UserLoginDto(
                "abc@aBc.ru", "312"
        )));
    }

    @Test
    void failureLoginWrongLogin() {
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.login(new UserLoginDto(
                "abc@aBsdc.ru", "312"
        )));
    }

    @Test
    void refreshTokenTest() {
        UserResponseDto user = authenticationService.login(new UserLoginDto(
                "abc@aBc.ru", "123"
        ));
        String refreshToken = "Bearer " + user.getRefreshToken();
        UserResponseDto newUser = authenticationService.refreshToken(refreshToken);
        assertAll(
                () -> assertNotNull(newUser.getRefreshToken()),
                () -> assertNotNull(newUser.getAccessToken()),
                () -> assertFalse(newUser.getRefreshToken().isEmpty()),
                () -> assertFalse(newUser.getAccessToken().isEmpty())
        );
    }
}
