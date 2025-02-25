package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.repository.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import com.plakhotnikov.cloud_storage_engine.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Сервис для аутентификации пользователей.
 * Обеспечивает вход в систему и обновление токенов.
 *
 * @see JwtService
 * @see UserRepository
 * @see AuthenticationManager
 * @see UserMapper
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;


    /**
     * Аутентифицирует пользователя и выдает JWT-токены.
     *
     * @param userLoginDto DTO с учетными данными пользователя.
     * @return DTO пользователя с токенами.
     * @throws BadCredentialsException если учетные данные неверны.
     */
    public UserResponseDto login(@RequestParam UserLoginDto userLoginDto) {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            UserEntity userEntity = (UserEntity) auth.getPrincipal();
            UserResponseDto result = userMapper.userToUserResponseDto(userEntity);
            result.setRefreshToken(jwtService.generateRefreshToken(userEntity));
            result.setAccessToken(jwtService.generateAccessToken(userEntity));
            return result;
        }
        catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }


    /**
     * Обновляет токены доступа на основе refresh-токена.
     *
     * @param header HTTP-заголовок с refresh-токеном.
     * @return DTO пользователя с обновленными токенами.
     * @throws BadCredentialsException если токен недействителен.
     * @throws JwtException если refresh-токен устарел или недействителен.
     */
    public UserResponseDto refreshToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token");
        }
        String token = header.substring(7);
        if (!jwtService.validateRefreshToken(token)){
            throw new JwtException("Refresh token does not valid");
        }
        var username = jwtService.getUsernameFromRefreshClaims(token);
        if (username.isEmpty()) {
            throw new JwtException("invalid refreshToken");
        }

        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("UserEntity does not exist"));
        if (jwtService.getIssuedAtFromRefreshClaims(token).isBefore(userEntity.getLastResetTime())) {
            throw new JwtException("password changed");
        }
        UserResponseDto responseDto = userMapper.userToUserResponseDto(userEntity);
        responseDto.setAccessToken(jwtService.generateAccessToken(userEntity));
        responseDto.setRefreshToken(jwtService.generateRefreshToken(userEntity));
        return responseDto;
    }
}
