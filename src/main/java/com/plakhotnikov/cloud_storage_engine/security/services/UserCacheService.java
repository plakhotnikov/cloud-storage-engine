package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class UserCacheService {
    @CachePut(value = "users", key = "#user.email")
    public UserEntity cacheUser(UserEntity user) {
        return user;
    }
}
