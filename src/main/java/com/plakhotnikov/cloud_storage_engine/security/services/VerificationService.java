package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.RoleRepository;
import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final StringRedisTemplate redisTemplate;
    private final long TOKEN_EXPIRATION_MINUTES = 15;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public String generateVerifyToken(String email) {
        String token = "V:" + UUID.randomUUID();
        redisTemplate.opsForValue().set(token, email, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    public String getEmailByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public String generateResetPasswordToken(String email) {
        String token = "RP:" + UUID.randomUUID();
        redisTemplate.opsForValue().set(token, email, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }


    @Transactional
    public UserResponseDto verifyEmail(String token) {
        if (!token.startsWith("V:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = getEmailByToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        if (!user.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " is already verified");
        }

        user.setAuthorities(List.of(roleRepository.findByRole("USER").get()));
        deleteToken(token);
        return userMapper.userToUserResponseDto(user);
    }
}
