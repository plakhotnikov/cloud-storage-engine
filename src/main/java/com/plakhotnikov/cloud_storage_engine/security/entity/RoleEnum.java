package com.plakhotnikov.cloud_storage_engine.security.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleEnum {
    ADMIN("ADMIN", 1L),
    USER("USER", 2L);

    private final String roleName;
    private final Long roleId;

    public static Role getEntityFromEnum(RoleEnum roleEnum) {
        return Role
                .builder()
                .role(roleEnum)
                .id(roleEnum.getRoleId())
                .build();
    }
}
