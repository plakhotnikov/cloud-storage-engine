package com.plakhotnikov.cloud_storage_engine.security;

import com.plakhotnikov.cloud_storage_engine.security.dto.UserLoginDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserRegistrationDto;
import com.plakhotnikov.cloud_storage_engine.security.dto.UserResponseDto;
import com.plakhotnikov.cloud_storage_engine.security.entity.Role;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    User loginDtoToUser(UserLoginDto loginDto);
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    User registrationDtoToUser(UserRegistrationDto registrationDto);


    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(source = "authorities", target = "roles", qualifiedByName = "mapRoles")
    UserResponseDto userToUserResponseDto(User user);


    @Named("mapRoles")
    default List<String> mapRoles(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(Role::getAuthority).collect(Collectors.toList());
    }
}
