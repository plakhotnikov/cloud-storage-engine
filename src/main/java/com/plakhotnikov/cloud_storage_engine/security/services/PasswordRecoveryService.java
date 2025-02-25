package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Сервис для восстановления пароля пользователя.
 * Позволяет сбрасывать и обновлять пароль в базе данных.
 *
 * @see TokenService
 * @see UserRepository
 * @see UserCacheService
 */
@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;


    /**
     * Сбрасывает пароль пользователя и обновляет его в базе данных.
     *
     * @param token Токен сброса пароля.
     * @param password Новый пароль пользователя.
     * @throws BadCredentialsException если токен недействителен.
     * @throws ResourceNotFoundException если пользователь не найден.
     */
    @Transactional
    public void resetPassword(String token, String password) {
        if (!token.startsWith("RP:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = tokenService.getEmailByToken(token);
        UserEntity user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(password));
        user = userRepository.save(user);
        userRepository.flush();

        userCacheService.cacheUser(user);

        tokenService.deleteToken(token);
    }
}
