package com.plakhotnikov.cloud_storage_engine.unit;

import com.plakhotnikov.cloud_storage_engine.security.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TokenServiceTest {
    @Autowired
    private TokenService tokenService;

    @Test
    public void testTokenService() {
        String verifyToken = tokenService.generateVerifyToken("email@gmail.com");
        String resetPasswordToken = tokenService.generateResetPasswordToken("email@gmail.com");
        assertEquals("email@gmail.com", tokenService.getEmailByToken(verifyToken));
        tokenService.deleteToken(verifyToken);
        assertNull(tokenService.getEmailByToken(verifyToken));
        assertEquals("email@gmail.com", tokenService.getEmailByToken(resetPasswordToken));
    }
}
