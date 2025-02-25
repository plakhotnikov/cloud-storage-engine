package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.security.repository.UserRepository;
import com.plakhotnikov.cloud_storage_engine.security.controller.AbstractSecuredController;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.storage.DirectorySpecifications;
import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.StorageMapper;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.CreateDirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления директориями в облачном хранилище.
 * Позволяет создавать, получать и проверять владельца директории.
 *
 * @see DirectoryRepository
 * @see UserRepository
 * @see StorageMapper
 */
@Service
@RequiredArgsConstructor
public class DirectoryService extends AbstractSecuredController {
    private final StorageMapper storageMapper;
    private final DirectoryRepository directoryRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт новую директорию.
     *
     * @param createDirectoryDto DTO с данными для создания директории.
     * @return DTO созданной директории.
     * @throws ResourceNotFoundException если пользователь или родительская директория не найдены.
     * @throws AccessDeniedException если пользователь не имеет доступа к созданию директории.
     * @see CreateDirectoryDto
     * @see DirectoryEntity
     */
    @Transactional
    public DirectoryDto createDirectory(CreateDirectoryDto createDirectoryDto) {

        if (createDirectoryDto.getParentDirectoryId() == null) {
            DirectoryEntity directoryEntity = DirectoryEntity.builder()
                    .name(createDirectoryDto.getName())
                    .owner(userRepository.findByEmail(getUserName())
                            .orElseThrow(() -> new ResourceNotFoundException("UserEntity with this email doesn't exist")))
                    .children(List.of())
                    .files(List.of())
                    .build();
            directoryEntity = directoryRepository.save(directoryEntity);
            return storageMapper.dirToDto(directoryEntity);
        }

        DirectoryEntity directoryEntity = directoryRepository.findById(createDirectoryDto.getParentDirectoryId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", createDirectoryDto.getParentDirectoryId())));
        UserEntity owner = directoryEntity.getOwner();
        if (!owner.getUsername().equals(getUserName()) ) {
            throw new AccessDeniedException("You do not have permission to access this directoryEntity");
        }
        DirectoryEntity subdirectory = directoryRepository.save(
                DirectoryEntity.builder()
                .name(createDirectoryDto.getName())
                .rootDirectory(directoryEntity)
                .owner(owner)
                .children(List.of())
                .files(List.of())
                .build()
        );

        directoryEntity.getChildren().add(subdirectory);
        directoryRepository.save(directoryEntity);
        return storageMapper.dirToDto(subdirectory);
    }

    /**
     * Получает список корневых директорий, принадлежащих пользователю с указанным именем.
     *
     * <p>Метод использует {@link Specification} для фильтрации директорий по двум критериям:
     * <ul>
     *     <li>Принадлежность указанному пользователю ({@link DirectorySpecifications#hasOwnerEmail(String)})</li>
     *     <li>Является ли директория корневой ({@link DirectorySpecifications#isRootDirectory()})</li>
     * </ul>
     *
     * @param username имя пользователя (обычно email), для которого необходимо найти корневые директории
     * @return список корневых директорий, принадлежащих указанному пользователю
     */
    private List<DirectoryEntity> getRootDirectories(String username) {
        Specification<DirectoryEntity> spec = Specification
                .where(DirectorySpecifications.hasOwnerEmail(username))
                .and(DirectorySpecifications.isRootDirectory());

        return directoryRepository.findAll(spec);
    }



    /**
     * Получает директорию по её ID.
     *
     * @param id ID директории.
     * @return DTO директории.
     * @throws ResourceNotFoundException если директория не найдена.
     * @see DirectoryDto
     */
    @Transactional
    public DirectoryDto getDirectoryById(Long id) {
        if (id == 0) {
            DirectoryDto directoryDto = new DirectoryDto();
            directoryDto.setName("root");
            var list = getRootDirectories(getUserName()).stream()
                    .map(storageMapper::dirToSubDto)
                    .toList();
            directoryDto.setChildren(list);
            return directoryDto;
        }
        return storageMapper.dirToDto(directoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", id))));
    }

    /**
     * Проверяет, является ли текущий пользователь владельцем директории.
     *
     * @param directoryId ID директории.
     * @return true, если пользователь владелец, иначе false.
     * @throws ResourceNotFoundException если директория не найдена.
     */
    @Transactional
    public boolean isDirectoryOwner(Long directoryId) {
        if (directoryId == 0) {
            return true;
        }
        DirectoryEntity directoryEntity = directoryRepository.findById(directoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", directoryId)));
        return directoryEntity.getOwner().getUsername().equals(getUserName());
    }


}
