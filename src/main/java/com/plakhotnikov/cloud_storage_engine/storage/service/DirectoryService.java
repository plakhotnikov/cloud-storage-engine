package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.security.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
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
    private final UserRepository userRepository;

    @Transactional
    public DirectoryDto createDirectory(CreateDirectoryDto createDirectoryDto) {

        if (createDirectoryDto.getParentDirectoryId() == null) {
            Directory directory = Directory.builder()
                    .name(createDirectoryDto.getName())
                    .owner(userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()) //todo на абстракный контроллер, который вытгивает пользователя из контекста и передаёт его в параметры
                            .orElseThrow(() -> new ResourceNotFoundException("User with this email doesn't exist")))
                    .children(List.of())
                    .files(List.of())
                    .build();
            directory = directoryRepository.save(directory);
            return storageMapper.dirToDto(directory);
        }

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
        directoryRepository.save(directory); // todo можешь поиграться с CascadeType = Persist  убрать второй сейв
        return storageMapper.dirToDto(subdirectory);
    }

    @Transactional
    public DirectoryDto getDirectory(Long id) { // getDirectoryById
        if (id == 0) {
            DirectoryDto directoryDto = new DirectoryDto();
            directoryDto.setName("root");
            directoryDto.setChildren(directoryRepository.findRootDirectories(SecurityContextHolder.getContext().getAuthentication().getName()).stream()
                    .map(storageMapper::dirToSubDto)
                    .toList());
            return directoryDto;
        }
        return storageMapper.dirToDto(directoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Directory with id '%s' not found", id))));
    }


    @Transactional
    public boolean isUserOwner(Long directoryId) { //todo нейминг
        if (directoryId == 0) {
            return true;
        }
        Directory directory = directoryRepository.findById(directoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Directory with id '%s' not found", directoryId)));
        return directory.getOwner().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }


}
