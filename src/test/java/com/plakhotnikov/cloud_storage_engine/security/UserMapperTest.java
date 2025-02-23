package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.Role;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    void mappingToUserResponseDto() {
        User user = new User(1L, "username","password", LocalDateTime.now(), List.of(new Role(1L, RoleEnum.USER)), List.of());
        UserResponseDto userResponseDto = userMapper.userToUserResponseDto(user);
        assertAll(
                () -> assertEquals(user.getUsername(), userResponseDto.getEmail()),
                () -> assertNull(userResponseDto.getAccessToken()),
                () -> assertNull(userResponseDto.getRefreshToken()),
                () -> assertEquals(user.getAuthorities().size(), userResponseDto.getRoles().size()),
                () -> assertEquals(user.getAuthorities().getFirst().getAuthority(), userResponseDto.getRoles().getFirst())
        );
    }

    @Test
    void loginDtoToUserEntity() {
        UserLoginDto userLoginDto = new UserLoginDto("username", "password");
        System.out.println(userLoginDto);
        User user = userMapper.loginDtoToUser(userLoginDto);
        System.out.println(user);
        assertAll(
                () -> assertEquals(user.getPassword(), userLoginDto.getPassword()),
                () -> assertEquals(user.getUsername(), userLoginDto.getEmail())
        );
    }

    @Test
    void registrationDtoToUserEntity() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto("username", "password");
        User user = userMapper.registrationDtoToUser(userRegistrationDto);
        assertAll(
                () -> assertEquals(user.getPassword(), userRegistrationDto.getPassword()),
                () -> assertEquals(user.getUsername(), userRegistrationDto.getEmail())
        );
    }
}
