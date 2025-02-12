package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import com.plakhotnikov.cloud_storage_engine.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final VerificationService verificationService;

    @Transactional
    public UserResponseDto registration(@RequestParam UserRegistrationDto userRegistrationDto) {
        User userToSave = userMapper.registrationDtoToUser(userRegistrationDto);
        userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));
        if (userRepository.existsByEmail(userToSave.getEmail())) {
            throw new UsernameNotFoundException("Email " + userToSave.getEmail() + " already exists");
        }

        userToSave = userRepository.save(userToSave);

        return userMapper.userToUserResponseDto(userToSave);
    }

    @Transactional
    public UserResponseDto login(@RequestParam UserLoginDto userLoginDto) {
        try {
            User user = userRepository.findByEmail(userLoginDto.getEmail()).orElseThrow(() -> new UsernameNotFoundException("Email " + userLoginDto.getEmail() + " not found"));
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserResponseDto result = userMapper.userToUserResponseDto(user);
            result.setRefreshToken(jwtService.generateRefreshToken(user));
            result.setAccessToken(jwtService.generateAccessToken(user));
            return result;
        }
        catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public UserResponseDto refreshToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
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

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        UserResponseDto responseDto = userMapper.userToUserResponseDto(user);
        responseDto.setAccessToken(jwtService.generateAccessToken(user));
        responseDto.setRefreshToken(jwtService.generateRefreshToken(user));
        return responseDto;
    }






    @Transactional
    public void resetPassword(String token, String password) {
        if (!token.startsWith("RP:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = verificationService.getEmailByToken(token);
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            verificationService.deleteToken(token);
        });
    }
}
