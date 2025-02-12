package com.plakhotnikov.cloud_storage_engine.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserRegistrationDto {
    private String email;
    private String password;
}
