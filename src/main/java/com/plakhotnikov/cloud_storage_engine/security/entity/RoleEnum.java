package com.plakhotnikov.cloud_storage_engine.security.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {
    ADMIN("ADMIN", 1L),
    USER("USER", 2L),;

    private final String roleName;
    private final Long roleId;

    public static RoleEntity getEntityFromEnum(RoleEnum roleEnum) {
        return RoleEntity.builder()
                .id(roleEnum.getRoleId())
                .role(roleEnum)
                .build();
    }


}
