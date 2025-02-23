package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import com.plakhotnikov.cloud_storage_engine.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    /**
     * @param userLoginDto // todo не заполнил
     * @return дто для логина {@link UserLoginDto}
     */
    public UserResponseDto login(@RequestParam UserLoginDto userLoginDto) {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            User user = (User) auth.getPrincipal();
            UserResponseDto result = userMapper.userToUserResponseDto(user);
            result.setRefreshToken(jwtService.generateRefreshToken(user));
            result.setAccessToken(jwtService.generateAccessToken(user));
            return result;
        }
        catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }


    /**
     * @param header
     * @return UserResponseDto which contains a list of authorities, access and refresh tokens
     */
    public UserResponseDto refreshToken(String header) {
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
        if (jwtService.getIssuedAtFromRefreshClaims(token).isBefore(user.getLastResetTime())) {
            throw new JwtException("password changed");
        }
        UserResponseDto responseDto = userMapper.userToUserResponseDto(user);
        responseDto.setAccessToken(jwtService.generateAccessToken(user));
        responseDto.setRefreshToken(jwtService.generateRefreshToken(user));
        return responseDto;
    }
}
