package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * Сервис для управления токенами верификации и сброса пароля.
 * Хранит токены в Redis с ограниченным сроком действия.
 *
 * @see StringRedisTemplate
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisRepository redisRepository;
    private final long TOKEN_EXPIRATION_MINUTES = 15;

    /**
     * Генерирует токен верификации для пользователя.
     *
     * @param email Email пользователя.
     * @return Сгенерированный токен верификации.
     */
    public String generateVerifyToken(String email) {
        String token = "V:" + UUID.randomUUID();
        redisRepository.save(token, email, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * Получает email пользователя по токену.
     *
     * @param token Токен верификации.
     * @return Email, связанный с токеном.
     */
    public String getEmailByToken(String token) {
        return redisRepository.find(token);
    }

    /**
     * Генерирует токен для сброса пароля.
     *
     * @param email Email пользователя.
     * @return Сгенерированный токен сброса пароля.
     */
    public String generateResetPasswordToken(String email) {
        String token = "RP:" + UUID.randomUUID();
        redisRepository.save(token, email, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return token;
    }


    /**
     * Удаляет токен из хранилища.
     *
     * @param token Токен для удаления.
     */
    public void deleteToken(String token) {
        redisRepository.delete(token);
    }



}
