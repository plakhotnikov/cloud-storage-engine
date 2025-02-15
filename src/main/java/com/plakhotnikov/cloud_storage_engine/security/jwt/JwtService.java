package com.plakhotnikov.cloud_storage_engine.security.jwt;

import com.plakhotnikov.cloud_storage_engine.properties.JwtProperties;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
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

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private SecretKey secretAccessKey;
    private SecretKey secretRefreshKey;

    @PostConstruct
    public void init() {
        secretAccessKey = Keys.hmacShaKeyFor(jwtProperties.getACCESS_SECRET_KEY().getBytes(StandardCharsets.UTF_8));
        secretRefreshKey = Keys.hmacShaKeyFor(jwtProperties.getREFRESH_SECRET_KEY().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param user
     * @return generated access token
     */
    public String generateAccessToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant =
                now.plusMinutes(jwtProperties.getACCESS_EXPIRATION_TIME())
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(accessExpiration)
                .setIssuedAt(new Date())
                .signWith(secretAccessKey)
                .claim("roles", user.getAuthorities())
                .compact();
    }

    /**
     * @param user
     * @return generated refresh token
     */
    public String generateRefreshToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant =
                now.plusMinutes(jwtProperties.getREFRESH_EXPIRATION_TIME())
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(refreshExpiration)
                .signWith(secretRefreshKey)
                .compact();
    }

    /**
     * @param accessToken
     * @return is accessToken valid
     */
    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, secretAccessKey);
    }

    /**
     * @param refreshToken
     * @return is refreshToken valid
     */
    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, secretRefreshKey);
    }


    /**
     * @param token
     * @param secret
     * @return is token valid with this key
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
     * @param token
     * @return username
     */
    public String getUsernameFromAccessClaims(String token) {
        return getClaims(token, secretAccessKey).getSubject();
    }


    /**
     * @param token
     * @return issuedAt
     */
    public LocalDateTime getIssuedAtFromAccessClaims(String token) {
        return getClaims(token, secretAccessKey).getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    /**
     * @param token
     * @return username
     */
    public String getUsernameFromRefreshClaims(String token) {
        return getClaims(token, secretRefreshKey).getSubject();
    }

    /**
     * @param token
     * @return issuedAt
     */
    public LocalDateTime getIssuedAtFromRefreshClaims(String token) {
        return getClaims(token, secretRefreshKey).getIssuedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * @param token
     * @param secret
     * @return claims
     */
    private Claims getClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
