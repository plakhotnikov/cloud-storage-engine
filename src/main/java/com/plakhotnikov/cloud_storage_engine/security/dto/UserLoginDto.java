package com.plakhotnikov.cloud_storage_engine.security.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.plakhotnikov.cloud_storage_engine.security.additional.LowerCaseDeserializer;
import jakarta.validation.constraints.Email;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserLoginDto {
    @Email
    @JsonDeserialize(using = LowerCaseDeserializer.class)
    private String email;
    private String password;
}
