package com.plakhotnikov.cloud_storage_engine.storage.repository;

import com.plakhotnikov.cloud_storage_engine.storage.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
}
