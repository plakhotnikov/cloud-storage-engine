package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEntity;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import com.plakhotnikov.cloud_storage_engine.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtServiceTest {
    @Autowired
    private JwtService jwtService;
    @Test
    public void accessTokenTest() {
        String token = jwtService.generateAccessToken(
                new UserEntity(1L,"abc@abc.ru", "123", LocalDateTime.now(), List.of(new RoleEntity(1L, RoleEnum.ADMIN), new RoleEntity(2L, RoleEnum.USER)), List.of())
        );
        assertAll(
                () -> assertTrue(jwtService.validateAccessToken(token)),
                () -> assertThrows(JwtException.class, () -> jwtService.validateRefreshToken(token)),
                () -> assertTrue(LocalDateTime.now().isAfter(jwtService.getIssuedAtFromAccessClaims(token))),
                () -> assertEquals(jwtService.getUsernameFromAccessClaims(token), "abc@abc.ru")
        );
    }
    @Test
    public void refreshTokenTest() {
        String token = jwtService.generateRefreshToken(
                new UserEntity(1L,"abc@abc.ru", "123", LocalDateTime.now(), List.of(new RoleEntity(1L, RoleEnum.ADMIN), new RoleEntity(2L, RoleEnum.USER)), List.of())
        );
        assertAll(
                () -> assertTrue(jwtService.validateRefreshToken(token)),
                () -> assertThrows(JwtException.class, () -> jwtService.validateAccessToken(token)),
                () -> assertTrue(LocalDateTime.now().isAfter(jwtService.getIssuedAtFromRefreshClaims(token))),
                () -> assertEquals(jwtService.getUsernameFromRefreshClaims(token), "abc@abc.ru")
        );
    }
}
