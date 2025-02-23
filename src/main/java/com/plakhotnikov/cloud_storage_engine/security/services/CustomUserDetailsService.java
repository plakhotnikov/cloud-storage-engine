package com.plakhotnikov.cloud_storage_engine.security.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final CachedUserService cachedUserService;


    /**
     * @param email the username identifying the user whose data is required.
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = null;
        user = cachedUserService.loadUserByUsername(email)
                 .orElse(
                         userRepository.findByEmail(email.toLowerCase())
                                 .orElseThrow(() -> new UsernameNotFoundException(email))
                 );

        cachedUserService.saveUser(user);

        return user;
    }

}
