package com.plakhotnikov.cloud_storage_engine.security;


import com.plakhotnikov.cloud_storage_engine.security.entity.Role;
import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(RoleEnum role);
}
