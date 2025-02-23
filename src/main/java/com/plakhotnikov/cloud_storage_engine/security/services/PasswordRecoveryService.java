package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CachedUserService cachedUserService;


    /** Reset password in database
     *
     * @param token
     * @param password
     */
    @Transactional
    public void resetPassword(String token, String password) {
        if (!token.startsWith("RP:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = tokenService.getEmailByToken(token);
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            tokenService.deleteToken(token);
        });
        cachedUserService.deleteUser(email);
    }
}
