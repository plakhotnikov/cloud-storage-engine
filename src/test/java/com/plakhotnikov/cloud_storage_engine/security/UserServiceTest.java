package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.services.TokenService;
import com.plakhotnikov.cloud_storage_engine.security.services.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

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
    void registrationInvalidEmailTest(){
        assertThrows(RuntimeException.class,
                () -> userService.registration(new UserRegistrationDto(
                        "123", "123"
                ))
        );
    }

    @Test
    void registrationTest(){
        UserResponseDto user = userService.registration(new UserRegistrationDto(
                "123@yandex.ru", "123"
        ));
        assertAll(
                () -> assertEquals("123@yandex.ru", user.getEmail()),
                () -> assertEquals(0, user.getRoles().size())
        );
    }

    @Test
    void verifyEmailTest(){

        String email = "1234@yandex.ru";
        String password = "1234";

        UserResponseDto user = userService.registration(new UserRegistrationDto(
                email, password
        ));
        String token = tokenService.generateVerifyToken(email);
        userService.verifyEmail(token);
        UserResponseDto user2 = userService.findByEmail(user.getEmail());
        assertAll(
                () -> assertEquals(email, user2.getEmail()),
                () -> assertEquals(1, user2.getRoles().size())
        );
    }



}
