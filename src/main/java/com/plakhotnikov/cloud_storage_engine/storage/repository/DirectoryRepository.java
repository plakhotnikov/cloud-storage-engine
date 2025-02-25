package com.plakhotnikov.cloud_storage_engine.storage.repository;

import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DirectoryRepository extends JpaRepository<DirectoryEntity, Long>, JpaSpecificationExecutor<DirectoryEntity> {
}
