package com.plakhotnikov.cloud_storage_engine.security.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final StringRedisTemplate redisTemplate;
    private final long TOKEN_EXPIRATION_MINUTES = 15;

    /**
     * @param email
     * @return verifyToken
     */
    public String generateVerifyToken(String email) {
        String token = "V:" + UUID.randomUUID();
        redisTemplate.opsForValue().set(token, email, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * @param token
     * @return email from token
     */
    public String getEmailByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    /**
     * @param email
     * @return reset password token
     */
    public String generateResetPasswordToken(String email) {
        String token = "RP:" + UUID.randomUUID();
        redisTemplate.opsForValue().set(token, email, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return token;
    }


    /** Delete token
     *
     * @param token
     */
    public void deleteToken(String token) {
        redisTemplate.delete(token); // todo не вынесено в репозиторий, создай RedisRepository
    }



}
