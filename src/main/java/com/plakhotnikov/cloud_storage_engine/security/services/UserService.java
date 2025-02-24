package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.RoleRepository;
import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEntity;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final CachedUserService cachedUserService;

    /**
     * Checks if user exist
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * @return UserResponseDto of UserEntity with param email
     */
    public UserResponseDto findByEmail(String email) {
        return  userMapper.userToUserResponseDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("UserEntity not found")));
    }


    /**
     * @param userRegistrationDto which contains email and password
     * @return UserResponseDto with info about user
     */
    @Transactional
    public UserResponseDto registration(@RequestParam UserRegistrationDto userRegistrationDto) {
        UserEntity userEntityToSave = userMapper.registrationDtoToUser(userRegistrationDto);
        userEntityToSave.setPassword(passwordEncoder.encode(userEntityToSave.getPassword()));
        userEntityToSave.setLastResetTime(LocalDateTime.now());
        if (userRepository.existsByEmail(userEntityToSave.getEmail())) {
            throw new UsernameNotFoundException("Email " + userEntityToSave.getEmail() + " already exists");
        }

        userEntityToSave = userRepository.save(userEntityToSave);

        return userMapper.userToUserResponseDto(userEntityToSave);
    }

    /**
     * If token is valid, gives to user role "USER"
     */
    @Transactional
    public UserResponseDto verifyEmail(String token) {
        if (!token.startsWith("V:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = tokenService.getEmailByToken(token);
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("UserEntity does not exist"));
        if (!userEntity.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("UserEntity with email " + email + " is already verified");
        }

        userEntity.getAuthorities().add(RoleEnum.getEntityFromEnum(RoleEnum.USER));

        tokenService.deleteToken(token);

        cachedUserService.deleteUser(email);
        return userMapper.userToUserResponseDto(userEntity);
    }

    /**
     * @return check if user is verified
     */
    public boolean isVerified(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("UserEntity not found"));
        return !userEntity.getAuthorities().isEmpty();
    }
}
