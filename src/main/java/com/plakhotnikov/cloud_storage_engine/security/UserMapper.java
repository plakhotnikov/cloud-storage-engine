package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEntity;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Маппер для преобразования сущностей пользователей и DTO.
 *
 * @see UserEntity
 * @see UserLoginDto
 * @see UserRegistrationDto
 * @see UserResponseDto
 * @see RoleEntity
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует DTO входа пользователя в сущность пользователя.
     *
     * @param loginDto DTO входа пользователя.
     * @return Сущность пользователя.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    UserEntity loginDtoToUser(UserLoginDto loginDto);

    /**
     * Преобразует DTO регистрации пользователя в сущность пользователя.
     *
     * @param registrationDto DTO регистрации пользователя.
     * @return Сущность пользователя.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    UserEntity registrationDtoToUser(UserRegistrationDto registrationDto);

    /**
     * Преобразует сущность пользователя в DTO ответа.
     *
     * @param userEntity Сущность пользователя.
     * @return DTO пользователя.
     */
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(source = "authorities", target = "roles", qualifiedByName = "mapRoles")
    UserResponseDto userToUserResponseDto(UserEntity userEntity);

    /**
     * Преобразует список ролей пользователя в список строковых представлений ролей.
     *
     * @param roleEntities Список сущностей ролей.
     * @return Список строковых представлений ролей.
     */
    @Named("mapRoles")
    default List<String> mapRoles(List<RoleEntity> roleEntities) {
        if (roleEntities == null) {
            return List.of();
        }
        return roleEntities.stream().map(RoleEntity::getAuthority).collect(Collectors.toList());
    }
}
