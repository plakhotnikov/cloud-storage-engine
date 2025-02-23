package com.plakhotnikov.cloud_storage_engine.storage.repository;

import com.plakhotnikov.cloud_storage_engine.storage.entity.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//todo Попробуй посмотреть что такое и как работает JpaSpecificationExecutor, criteriaBuilder
public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    @Query("SELECT d from Directory d WHERE d.owner.email = :username AND d.rootDirectory IS NULL")
    List<Directory> findRootDirectories(String username);
}
