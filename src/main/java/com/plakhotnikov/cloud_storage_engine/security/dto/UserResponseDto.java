package com.plakhotnikov.cloud_storage_engine.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserResponseDto {
    private String email;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
}
