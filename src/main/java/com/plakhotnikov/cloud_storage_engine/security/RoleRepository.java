package com.plakhotnikov.cloud_storage_engine.security;


import com.plakhotnikov.cloud_storage_engine.security.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

}
