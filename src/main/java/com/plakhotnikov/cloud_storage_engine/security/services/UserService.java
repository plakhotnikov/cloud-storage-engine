package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.RoleRepository;
import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import com.plakhotnikov.cloud_storage_engine.security.exception.ResourceNotFoundException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
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

    // TODO: 15.02.2025 JAVADOC 
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserResponseDto findByEmail(String email) {
        return  userMapper.userToUserResponseDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }


    @Transactional
    public UserResponseDto registration(@RequestParam UserRegistrationDto userRegistrationDto) {
        User userToSave = userMapper.registrationDtoToUser(userRegistrationDto);
        userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));
        userToSave.setLastResetTime(LocalDateTime.now());
        if (userRepository.existsByEmail(userToSave.getEmail())) {
            throw new UsernameNotFoundException("Email " + userToSave.getEmail() + " already exists");
        }

        userToSave = userRepository.save(userToSave);

        return userMapper.userToUserResponseDto(userToSave);
    }

    @Transactional
    public UserResponseDto verifyEmail(String token) {
        if (!token.startsWith("V:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = tokenService.getEmailByToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        if (!user.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " is already verified");
        }

        user.setAuthorities(List.of(roleRepository.findByRole("USER").orElseThrow(
                () -> new RuntimeException("Role USER not found")
        )));
        tokenService.deleteToken(token);
        return userMapper.userToUserResponseDto(user);
    }

    public boolean isVerified(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return !user.getAuthorities().isEmpty();
    }
}
