package com.plakhotnikov.cloud_storage_engine.security.jwt;

import com.plakhotnikov.cloud_storage_engine.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
//    private Key accessKey = Keys.hmacShaKeyFor(jwtProperties.getACCESS_SECRET_KEY().getBytes(StandardCharsets.UTF_8));
//    private Key refreshKey = Keys.hmacShaKeyFor(jwtProperties.getREFRESH_SECRET_KEY().getBytes(StandardCharsets.UTF_8));

//    public String generateAccessToken(String username) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getACCESS_EXPIRATION_TIME() * 60000))
//                .signWith(accessKey)
//                .compact();
//    }
//    public String generateRefreshToken(String username) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getREFRESH_EXPIRATION_TIME() * 60000))
//                .signWith(refreshKey)
//                .compact();
//    }
}
