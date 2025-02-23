package com.plakhotnikov.cloud_storage_engine.security.services;

import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.security.RoleRepository;
import com.plakhotnikov.cloud_storage_engine.security.UserMapper;
import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

import static com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum.getEntityFromEnum;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CachedUserService cachedUserService;

    /**
     * Checks if user exist
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * @return UserResponseDto of User with param email
     */
    public UserResponseDto findByEmail(String email) {
        return userMapper.userToUserResponseDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }


    /**
     * @param userRegistrationDto which contains email and password
     * @return UserResponseDto with info about user
     */
    @Transactional  //todo подумать нуна ли здесь вообще транзакция или же нет
    public UserResponseDto registration(@RequestParam UserRegistrationDto userRegistrationDto) {
        User userToSave = userMapper.registrationDtoToUser(userRegistrationDto);
        userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));
        userToSave.setLastResetTime(LocalDateTime.now());
        if (userRepository.existsByEmail(userToSave.getEmail())) {
            throw new UsernameNotFoundException("Email " + userToSave.getEmail() + " already exists");
        }

        return userMapper.userToUserResponseDto(userRepository.save(userToSave));
    }

    /**
     * If token is valid, gives to user role "USER"
     */
    @Transactional // подумай нужна ли тебе транзакция
    public UserResponseDto verifyEmail(String token) {
        if (!token.startsWith("V:")) {
            throw new BadCredentialsException("Invalid token");
        }
        String email = tokenService.getEmailByToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        if (!user.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " is already verified");
        }

        //todo нахуй не надо делать запрос, ибо роли выносим в енам и просто дублируем в бд
        user.getAuthorities()
                .add(getEntityFromEnum(RoleEnum.USER));

        // лучше явно написать что сохраняем пользователя
        // hibernate dirty checking - commit транзакции

        tokenService.deleteToken(token);

        cachedUserService.deleteUser(email); //todo хуйня посмотри на аннотацию @Cachable @CachePut, но здесь пиздец ёбля с тем, как это всё evicti ть
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * @return check if user is verified
     */
    public boolean isVerified(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return !user.getAuthorities().isEmpty();
    }
}
