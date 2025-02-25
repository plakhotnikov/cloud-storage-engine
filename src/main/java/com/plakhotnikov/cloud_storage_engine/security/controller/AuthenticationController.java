package com.plakhotnikov.cloud_storage_engine.security.controller;

import com.plakhotnikov.cloud_storage_engine.security.dto.ResetPasswordDto;
import com.plakhotnikov.cloud_storage_engine.security.services.*;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
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
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Аутентифицирует пользователя и возвращает JWT-токены.
     *
     * @param loginDto DTO с учетными данными пользователя.
     * @return DTO пользователя с токенами и ролями.
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT-токенов")
    public UserResponseDto login(@RequestBody UserLoginDto loginDto) {
        return authenticationService.login(loginDto);
    }

    /**
     * Регистрирует нового пользователя и отправляет письмо для подтверждения email.
     *
     * @param registrationDto DTO с данными регистрации.
     * @return DTO зарегистрированного пользователя.
     */
    @PostMapping("/registration")
    @Operation(summary = "Регистрация пользователя", description = "Создание нового пользователя")
    public UserResponseDto registration(@RequestBody UserRegistrationDto registrationDto) {
        return userService.registration(registrationDto);
    }


    /**
     * Обновляет access и refresh токены.
     *
     * @param request HTTP-запрос с заголовком авторизации.
     * @return DTO пользователя с обновленными токенами.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Обновление токенов", description = "Обновление access и refresh токенов пользователя")
    public UserResponseDto refresh(HttpServletRequest request) {
        return authenticationService.refreshToken(request.getHeader("Authorization"));
    }

    /**
     * Отправляет письмо с подтверждением email, если аккаунт не верифицирован.
     *
     * @param email Email пользователя.
     * @return Ответ 200 OK, если письмо отправлено.
     * @throws UsernameNotFoundException если аккаунт уже верифицирован.
     */
    @GetMapping("/send-verification-email")
    @Operation(summary = "Отправка письма для подтверждения email", description = "Отправляет письмо с подтверждением email, если аккаунт не верифицирован")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) {
        if (!userService.isVerified(email)) {
            emailService.sendVerificationEmail(email, tokenService.generateVerifyToken(email));
            return ResponseEntity.ok().build();
        }
        throw new UsernameNotFoundException(email);
    }

    /**
     * Подтверждает email пользователя.
     *
     * @param token Токен подтверждения email.
     * @return DTO пользователя с назначенными ролями.
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Подтверждение email", description = "Подтверждает email пользователя с использованием токена")
    public UserResponseDto verifyEmail(@RequestParam String token) {
        return userService.verifyEmail(token);
    }


    /**
     * Отправляет письмо для сброса пароля.
     *
     * @param email Email пользователя.
     * @return Ответ 200 OK, если письмо отправлено.
     * @throws UsernameNotFoundException если пользователь не найден.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Запрос на сброс пароля", description = "Отправляет письмо с инструкцией по сбросу пароля")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) {
        customUserDetailsService.loadUserByUsername(email);
        emailService.sendResetPasswordEmail(email, tokenService.generateResetPasswordToken(email));
        return ResponseEntity.ok().build();
    }


    /**
     * Сбрасывает пароль пользователя с использованием токена сброса.
     *
     * @param resetPasswordDto DTO с токеном и новым паролем.
     * @return Ответ 200 OK после успешного сброса пароля.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Сброс пароля", description = "Позволяет пользователю сменить пароль с использованием токена сброса пароля")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        passwordRecoveryService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
        return ResponseEntity.ok().build();

    }



}
