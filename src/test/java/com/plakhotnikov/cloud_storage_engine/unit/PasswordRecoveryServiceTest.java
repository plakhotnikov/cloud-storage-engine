package com.plakhotnikov.cloud_storage_engine.unit;

import com.plakhotnikov.cloud_storage_engine.security.services.CustomUserDetailsService;
import com.plakhotnikov.cloud_storage_engine.security.services.PasswordRecoveryService;
import com.plakhotnikov.cloud_storage_engine.security.services.TokenService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PasswordRecoveryServiceTest {
    @Autowired
    private PasswordRecoveryService passwordRecoveryService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void testResetPassword() {
        String token = tokenService.generateResetPasswordToken("abc@abc.ru");
        passwordRecoveryService.resetPassword(token, "12345");
        UserDetails user = userService.loadUserByUsername("abc@abc.ru");

        assertTrue(passwordEncoder.matches("12345", user.getPassword()));

    }
}
