package com.plakhotnikov.cloud_storage_engine.security.controller;

import com.plakhotnikov.cloud_storage_engine.security.dto.ResetPasswordDto;
import com.plakhotnikov.cloud_storage_engine.security.services.*;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/login")
    public UserResponseDto login(@RequestBody UserLoginDto loginDto) {
        return authenticationService.login(loginDto);
    }

    @PostMapping("/registration")
    public UserResponseDto registration(@RequestBody UserRegistrationDto registrationDto) {
        UserResponseDto userResponseDto = userService.registration(registrationDto);
        emailService.sendVerificationEmail(userResponseDto.getEmail(), tokenService.generateVerifyToken(userResponseDto.getEmail()));
        return userResponseDto;
    }

    @PostMapping("/refresh")
    public UserResponseDto refresh(HttpServletRequest request) {
        return authenticationService.refreshToken(request.getHeader("Authorization"));
    }

    @GetMapping("send-verification-email")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) {

        if (userService.findByEmail(email).getRoles().isEmpty()) {
            emailService.sendVerificationEmail(email, tokenService.generateVerifyToken(email));
            return ResponseEntity.ok().build();
        }
        throw new UsernameNotFoundException(email);
    }

    @GetMapping("/verify-email")
    public UserResponseDto verifyEmail(@RequestParam String token) {
        return userService.verifyEmail(token);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) {
        if (userService.existsByEmail(email)) {
            emailService.sendResetPasswordEmail(email, tokenService.generateResetPasswordToken(email));
            return ResponseEntity.ok().build();
        }
        throw new UsernameNotFoundException(email);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        passwordRecoveryService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
        return ResponseEntity.ok().build();
    }



}
