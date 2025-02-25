package com.plakhotnikov.cloud_storage_engine.security.jwt;

import com.plakhotnikov.cloud_storage_engine.properties.JwtProperties;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


/**
 * Сервис для работы с JWT-токенами (доступа и обновления).
 *
 * @see JwtProperties
 * @see UserEntity
 * @see JwtException
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private SecretKey secretAccessKey;
    private SecretKey secretRefreshKey;

    /**
     * Инициализирует секретные ключи для подписи токенов.
     */
    @PostConstruct
    public void init() {
        secretAccessKey = Keys.hmacShaKeyFor(jwtProperties.getACCESS_SECRET_KEY().getBytes(StandardCharsets.UTF_8));
        secretRefreshKey = Keys.hmacShaKeyFor(jwtProperties.getREFRESH_SECRET_KEY().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует access-токен для пользователя.
     *
     * @param userEntity Пользователь.
     * @return Сгенерированный access-токен.
     */
    public String generateAccessToken(UserEntity userEntity) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant =
                now.plusMinutes(jwtProperties.getACCESS_EXPIRATION_TIME())
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);
        return Jwts.builder()
                .setSubject(userEntity.getUsername())
                .setExpiration(accessExpiration)
                .setIssuedAt(new Date())
                .signWith(secretAccessKey)
                .claim("roles", userEntity.getAuthorities())
                .compact();
    }

    /**
     * Генерирует refresh-токен для пользователя.
     *
     * @param userEntity Пользователь.
     * @return Сгенерированный refresh-токен.
     */
    public String generateRefreshToken(UserEntity userEntity) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant =
                now.plusMinutes(jwtProperties.getREFRESH_EXPIRATION_TIME())
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        return Jwts.builder()
                .setSubject(userEntity.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(refreshExpiration)
                .signWith(secretRefreshKey)
                .compact();
    }

    /**
     * Проверяет валидность access-токена.
     *
     * @param accessToken Токен доступа.
     * @return true, если токен валиден, иначе выбрасывает исключение.
     */
    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, secretAccessKey);
    }

    /**
     * Проверяет валидность refresh-токена.
     *
     * @param refreshToken Токен обновления.
     * @return true, если токен валиден, иначе выбрасывает исключение.
     */
    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, secretRefreshKey);
    }


    /**
     * Проверяет валидность токена с использованием указанного ключа.
     *
     * @param token JWT-токен.
     * @param secret Секретный ключ для проверки подписи.
     * @return true, если токен валиден, иначе выбрасывает исключение.
     */
    private boolean validateToken(String token, SecretKey secret) {
        try {
            Jwts
                    .parser()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        catch (Exception e){
            throw new JwtException(e.getMessage());
        }
    }

    /**
     * Извлекает имя пользователя из access-токена.
     *
     * @param token JWT-токен.
     * @return Имя пользователя.
     */
    public String getUsernameFromAccessClaims(String token) {
        return getClaims(token, secretAccessKey).getSubject();
    }


    /**
     * Получает дату выпуска access-токена.
     *
     * @param token JWT-токен.
     * @return Дата выпуска токена.
     */
    public LocalDateTime getIssuedAtFromAccessClaims(String token) {
        return getClaims(token, secretAccessKey).getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    /**
     * Извлекает имя пользователя из refresh-токена.
     *
     * @param token JWT-токен.
     * @return Имя пользователя.
     */
    public String getUsernameFromRefreshClaims(String token) {
        return getClaims(token, secretRefreshKey).getSubject();
    }

    /**
     * Получает дату выпуска refresh-токена.
     *
     * @param token JWT-токен.
     * @return Дата выпуска токена.
     */
    public LocalDateTime getIssuedAtFromRefreshClaims(String token) {
        return getClaims(token, secretRefreshKey).getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Извлекает Claims (данные) из JWT-токена.
     *
     * @param token JWT-токен.
     * @param secret Секретный ключ для подписи.
     * @return Claims, содержащие информацию из токена.
     */
    private Claims getClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
