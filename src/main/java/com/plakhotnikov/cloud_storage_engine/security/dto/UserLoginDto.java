package com.plakhotnikov.cloud_storage_engine.security.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserLoginDto {
    private String email;
    private String password;
}
