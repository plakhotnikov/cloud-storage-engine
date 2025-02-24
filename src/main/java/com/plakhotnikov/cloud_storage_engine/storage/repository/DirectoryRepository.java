package com.plakhotnikov.cloud_storage_engine.storage.repository;

import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DirectoryRepository extends JpaRepository<DirectoryEntity, Long> {
    @Query("SELECT d from DirectoryEntity d WHERE d.owner.email = :username AND d.rootDirectoryEntity IS NULL")
    List<DirectoryEntity> findRootDirectories(String username);
}
