package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEntity;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class UserEntityMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    void mappingToUserResponseDto() {
        UserEntity userEntity = new UserEntity(1L, "username","password", LocalDateTime.now(), List.of(new RoleEntity(1L, RoleEnum.USER)), List.of());
        UserResponseDto userResponseDto = userMapper.userToUserResponseDto(userEntity);
        assertAll(
                () -> assertEquals(userEntity.getUsername(), userResponseDto.getEmail()),
                () -> assertNull(userResponseDto.getAccessToken()),
                () -> assertNull(userResponseDto.getRefreshToken()),
                () -> assertEquals(userEntity.getAuthorities().size(), userResponseDto.getRoles().size()),
                () -> assertEquals(userEntity.getAuthorities().getFirst().getAuthority(), userResponseDto.getRoles().getFirst())
        );
    }

    @Test
    void loginDtoToUserEntity() {
        UserLoginDto userLoginDto = new UserLoginDto("username", "password");
        System.out.println(userLoginDto);
        UserEntity userEntity = userMapper.loginDtoToUser(userLoginDto);
        System.out.println(userEntity);
        assertAll(
                () -> assertEquals(userEntity.getPassword(), userLoginDto.getPassword()),
                () -> assertEquals(userEntity.getUsername(), userLoginDto.getEmail())
        );
    }

    @Test
    void registrationDtoToUserEntity() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto("username", "password");
        UserEntity userEntity = userMapper.registrationDtoToUser(userRegistrationDto);
        assertAll(
                () -> assertEquals(userEntity.getPassword(), userRegistrationDto.getPassword()),
                () -> assertEquals(userEntity.getUsername(), userRegistrationDto.getEmail())
        );
    }
}
