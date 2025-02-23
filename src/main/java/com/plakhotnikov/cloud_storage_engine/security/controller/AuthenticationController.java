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

    /**
     * @param loginDto //todo не заполнил
     * @return userResponseDto with access, refresh tokens and list of authorities
     */
    @PostMapping("/login")
    public UserResponseDto login(@RequestBody UserLoginDto loginDto) {
        return authenticationService.login(loginDto);
    }

    /**
     * @param registrationDto //todo не заполнил
     * @action send you verify email message //todo fix
     * @return userResponseDto which contains username
     */
    @PostMapping("/registration")
    public UserResponseDto registration(@RequestBody UserRegistrationDto registrationDto) {
        UserResponseDto userResponseDto = userService.registration(registrationDto);
        emailService.sendVerificationEmail(userResponseDto.getEmail(), tokenService.generateVerifyToken(userResponseDto.getEmail()));
        return userResponseDto;
    }


    /**
     * @param request
     * @return UserResponseDto with new access and refresh tokens
     */
    @PostMapping("/refresh")
    public UserResponseDto refresh(HttpServletRequest request) {
        return authenticationService.refreshToken(request.getHeader("Authorization"));
    }

    /**
     * @param email
     * @action send you email verify message if your account isn't verified
     */
    @GetMapping("send-verification-email")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) {
        if (!userService.isVerified(email)) {
            emailService.sendVerificationEmail(email, tokenService.generateVerifyToken(email));
            return ResponseEntity.ok().build();
        }
        throw new UsernameNotFoundException(email);
    }

    /**
     * @param token
     * @return UserResponseDto with a list of authorities, which given while verification
     */
    @GetMapping("/verify-email")
    public UserResponseDto verifyEmail(@RequestParam String token) {
        return userService.verifyEmail(token);
    }


    /**
     * @param email
     * @action Send to your email reset password message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) {
        if (userService.existsByEmail(email)) {
            emailService.sendResetPasswordEmail(email, tokenService.generateResetPasswordToken(email));
            return ResponseEntity.ok().build();
        }
        throw new UsernameNotFoundException(email);
    }


    /**
     * @param resetPasswordDto
     * Reset password using reset-password-token which you can get in func forgotPassword
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        passwordRecoveryService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
        return ResponseEntity.ok().build();

    }



}
