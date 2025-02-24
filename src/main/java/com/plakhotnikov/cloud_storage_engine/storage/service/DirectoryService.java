package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
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
    private final UserRepository userRepository;

    @Transactional
    public DirectoryDto createDirectory(CreateDirectoryDto createDirectoryDto) {

        if (createDirectoryDto.getParentDirectoryId() == null) {
            DirectoryEntity directoryEntity = DirectoryEntity.builder()
                    .name(createDirectoryDto.getName())
                    .owner(userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                            .orElseThrow(() -> new ResourceNotFoundException("UserEntity with this email doesn't exist")))
                    .children(List.of())
                    .fileEntities(List.of())
                    .build();
            directoryEntity = directoryRepository.save(directoryEntity);
            return storageMapper.dirToDto(directoryEntity);
        }

        DirectoryEntity directoryEntity = directoryRepository.findById(createDirectoryDto.getParentDirectoryId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", createDirectoryDto.getParentDirectoryId())));
        UserEntity owner = directoryEntity.getOwner();
        if (!owner.getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName()) ) {
            throw new AccessDeniedException("You do not have permission to access this directoryEntity");
        }
        DirectoryEntity subdirectory = directoryRepository.save(
                DirectoryEntity.builder()
                .name(createDirectoryDto.getName())
                .rootDirectoryEntity(directoryEntity)
                .owner(owner)
                .children(List.of())
                .fileEntities(List.of())
                .build()
        );

        directoryEntity.getChildren().add(subdirectory);
        directoryRepository.save(directoryEntity);
        return storageMapper.dirToDto(subdirectory);
    }

    @Transactional
    public DirectoryDto getDirectoryById(Long id) {
        if (id == 0) {
            DirectoryDto directoryDto = new DirectoryDto();
            directoryDto.setName("root");
            directoryDto.setChildren(directoryRepository.findRootDirectories(SecurityContextHolder.getContext().getAuthentication().getName()).stream()
                    .map(storageMapper::dirToSubDto)
                    .toList());
            return directoryDto;
        }
        return storageMapper.dirToDto(directoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", id))));
    }


    @Transactional
    public boolean isDirectoryOwner(Long directoryId) {
        if (directoryId == 0) {
            return true;
        }
        DirectoryEntity directoryEntity = directoryRepository.findById(directoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", directoryId)));
        return directoryEntity.getOwner().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }


}
