package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.repository.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Сервис для управления пользователями, включая регистрацию и верификацию.
 *
 * @see UserMapper
 * @see UserRepository
 * @see TokenService
 * @see UserCacheService
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserCacheService userCacheService;


    /**
     * Регистрирует нового пользователя.
     *
     * @param userRegistrationDto DTO с данными для регистрации пользователя.
     * @return DTO зарегистрированного пользователя.
     * @throws UsernameNotFoundException если email уже существует в системе.
     */
    @Transactional
    public UserResponseDto registration(@RequestParam UserRegistrationDto userRegistrationDto) {
        UserEntity userEntityToSave = userMapper.registrationDtoToUser(userRegistrationDto);
        userEntityToSave.setPassword(passwordEncoder.encode(userEntityToSave.getPassword()));
        if (userRepository.existsByEmail(userEntityToSave.getEmail())) {
            throw new UsernameNotFoundException("Email " + userEntityToSave.getEmail() + " already exists");
        }

        userEntityToSave = userRepository.save(userEntityToSave);

        return userMapper.userToUserResponseDto(userEntityToSave);
    }


    /**
     * Верифицирует email пользователя и присваивает ему роль "USER".
     *
     * @param token Токен верификации.
     * @return DTO обновленного пользователя.
     * @throws BadCredentialsException если токен недействителен.
     * @throws UsernameNotFoundException если пользователь уже верифицирован или не существует.
     */
    @Transactional
    public UserResponseDto verifyEmail(String token) {
        if (!token.startsWith("V:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = tokenService.getEmailByToken(token);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("UserEntity does not exist"));
        if (!userEntity.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("UserEntity with email " + email + " is already verified");
        }

        userEntity.getAuthorities().add(RoleEnum.getEntityFromEnum(RoleEnum.USER));

        tokenService.deleteToken(token);
        userEntity = userRepository.save(userEntity);

        userCacheService.cacheUser(userEntity);

        return userMapper.userToUserResponseDto(userEntity);
    }

    /**
     * Проверяет, верифицирован ли пользователь.
     *
     * @param email Email пользователя.
     * @return true, если пользователь верифицирован, иначе false.
     */
    public boolean isVerified(String email) {
        UserEntity userEntity = (UserEntity) customUserDetailsService.loadUserByUsername(email);
        return !userEntity.getAuthorities().isEmpty();
    }
}
