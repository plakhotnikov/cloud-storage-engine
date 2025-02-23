package com.plakhotnikov.cloud_storage_engine.security.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CachedUserService {
    private static final String USER_PREFIX = "USER_";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;


    Optional<User> loadUserByUsername(String email) {
        String key = USER_PREFIX + email;
        String userJson = (String) redisTemplate.opsForValue().get(key);
        if (userJson != null) {
            try {
                return Optional.ofNullable(objectMapper.readValue(userJson, User.class));
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Ошибка десериализации пользователя", e);
            }
        }
        return Optional.empty();
    }

    void saveUser(User user) {
        try {
            String key = USER_PREFIX + user.getEmail();
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(user), Duration.ofMinutes(10));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(String email) {
        redisTemplate.delete(USER_PREFIX + email);
    }
}
