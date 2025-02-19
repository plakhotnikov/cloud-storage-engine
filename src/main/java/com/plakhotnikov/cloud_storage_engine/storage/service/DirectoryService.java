package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import com.plakhotnikov.cloud_storage_engine.security.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.storage.entity.Directory;
import com.plakhotnikov.cloud_storage_engine.storage.entity.StorageMapper;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.CreateDirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final StorageMapper storageMapper;
    private final DirectoryRepository directoryRepository;

    @Transactional
    DirectoryDto createDirectory(CreateDirectoryDto createDirectoryDto) {

        Directory directory = directoryRepository.findById(createDirectoryDto.getParentDirectoryId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Directory with id '%s' not found", createDirectoryDto.getParentDirectoryId())));
        User owner = directory.getOwner();
        if (!owner.getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ) {
            throw new AccessDeniedException("You do not have permission to access this directory");
        }
        Directory subdirectory = directoryRepository.save(
                Directory.builder()
                .name(createDirectoryDto.getName())
                .rootDirectory(directory)
                .owner(owner)
                .children(List.of())
                .files(List.of())
                .build()
        );

        directory.getChildren().add(subdirectory);
//        directoryRepository.save(directory);
        return storageMapper.dirToDto(subdirectory);
    }
}
