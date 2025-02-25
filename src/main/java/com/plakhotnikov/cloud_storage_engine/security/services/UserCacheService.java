package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;


/**
 * Сервис кеширования пользователей.
 * Позволяет сохранять пользователей в кеш для быстрого доступа.
 *
 * @see UserEntity
 */
@Service
public class UserCacheService {

    /**
     * Кеширует пользователя по его email.
     *
     * @param user Пользователь, которого нужно закешировать.
     * @return Закешированный пользователь.
     */
    @CachePut(value = "users", key = "#user.email")
    public UserEntity cacheUser(UserEntity user) {
        return user;
    }
}
