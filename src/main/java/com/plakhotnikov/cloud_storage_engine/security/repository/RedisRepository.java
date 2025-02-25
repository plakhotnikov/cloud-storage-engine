package com.plakhotnikov.cloud_storage_engine.security.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Репозиторий для работы с Redis с использованием {@link StringRedisTemplate}.
 * Позволяет сохранять, извлекать и удалять строки по ключу.
 */
@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final StringRedisTemplate redisTemplate;

    /**
     * Сохраняет значение в Redis с указанным ключом и временем жизни.
     *
     * @param key      ключ, по которому будет сохранено значение
     * @param value    сохраняемое значение
     * @param timeout  время жизни ключа в хранилище
     * @param timeUnit единица измерения времени жизни (секунды, минуты и т. д.)
     */
    public void save(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * Извлекает значение из Redis по заданному ключу.
     *
     * @param key ключ, по которому будет найдено значение
     * @return сохранённое значение или {@code null}, если ключ отсутствует
     */
    public String find(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Удаляет запись из Redis по ключу.
     *
     * @param key ключ, который необходимо удалить
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
