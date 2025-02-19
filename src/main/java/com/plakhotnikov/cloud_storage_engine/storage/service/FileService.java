package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.storage.entity.StorageMapper;
import com.plakhotnikov.cloud_storage_engine.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {
    private final StorageMapper storageMapper;
    private final FileRepository fileRepository;
    private final MinioService minioService;

}
