package com.plakhotnikov.cloud_storage_engine.unit;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthenticationServiceTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    public void loginTest() {
        UserResponseDto user = authenticationService.login(new UserLoginDto("abc@abc.ru", "123"));
        assertAll(
                () -> assertNotNull(user.getAccessToken()),
                () -> assertNotNull(user.getRefreshToken()),
                () -> assertEquals(user.getRoles().size(), 2),
                () -> assertEquals(user.getEmail(), "abc@abc.ru")
        );
    }

    @Test
    public void registrationTest() {
        UserResponseDto user = authenticationService.registration(new UserRegistrationDto("emailservicecloudstorage@gmail.com", "password"));
        assertAll(
                () -> assertNull(user.getAccessToken()),
                () -> assertNull(user.getRefreshToken()),
                () -> assertEquals(user.getEmail(), "emailservicecloudstorage@gmail.com")
        );
    }
}
