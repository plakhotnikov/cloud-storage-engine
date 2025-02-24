package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEntity;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    UserEntity loginDtoToUser(UserLoginDto loginDto);
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    UserEntity registrationDtoToUser(UserRegistrationDto registrationDto);


    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(source = "authorities", target = "roles", qualifiedByName = "mapRoles")
    UserResponseDto userToUserResponseDto(UserEntity userEntity);


    @Named("mapRoles")
    default List<String> mapRoles(List<RoleEntity> roleEntities) {
        if (roleEntities == null) {
            return List.of();
        }
        return roleEntities.stream().map(RoleEntity::getAuthority).collect(Collectors.toList());
    }
}
