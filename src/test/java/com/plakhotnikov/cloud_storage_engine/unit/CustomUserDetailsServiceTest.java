package com.plakhotnikov.cloud_storage_engine.unit;

import com.plakhotnikov.cloud_storage_engine.security.services.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CustomUserDetailsServiceTest {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loadUserByUsername() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("abc@abc.ru");
        assertAll(
                () -> assertEquals(userDetails.getUsername(), "abc@abc.ru"),
                () -> assertEquals(2, userDetails.getAuthorities().size())
        );
    }

}
