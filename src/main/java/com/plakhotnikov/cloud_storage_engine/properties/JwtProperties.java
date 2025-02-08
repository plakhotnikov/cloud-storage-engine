package com.plakhotnikov.cloud_storage_engine.properties;


import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@ToString
public class JwtProperties {
    @Value("${jwt.access-secret-key}")
    private String ACCESS_SECRET_KEY;
    @Value("${jwt.refresh-secret-key}")
    private String REFRESH_SECRET_KEY;
    @Value("${jwt.access-expiration-time}")
    private long ACCESS_EXPIRATION_TIME; // in minutes
    @Value("${jwt.refresh-expiration-time}")
    private long REFRESH_EXPIRATION_TIME; // in minutes
}
