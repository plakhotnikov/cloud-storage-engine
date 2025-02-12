package com.plakhotnikov.cloud_storage_engine.unit;

import com.plakhotnikov.cloud_storage_engine.security.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {
    @Autowired
    private EmailService emailService;

    @Test
    void sendVerificationEmail() {
        emailService.sendVerificationEmail("emailservicecloudstorage@gmail.com", "here will be your token");
    }

    @Test
    void sendPasswordResetEmail() {
        emailService.sendResetPasswordEmail("emailservicecloudstorage@gmail.com", "here will be your token");
    }
}
