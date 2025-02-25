package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.security.controller.AbstractSecuredController;
import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.FileEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.StorageMapper;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.FileDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.MoveFileDto;
import com.plakhotnikov.cloud_storage_engine.storage.repository.DirectoryRepository;
import com.plakhotnikov.cloud_storage_engine.storage.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.plakhotnikov.cloud_storage_engine.util.FileUtil;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления файлами в облачном хранилище.
 * Позволяет загружать, удалять, скачивать и перемещать файлы.
 *
 * @see MinioService
 * @see FileRepository
 * @see DirectoryRepository
 */
@Service
@RequiredArgsConstructor
public class FileService extends AbstractSecuredController {
    private final FileRepository fileRepository;
    private final DirectoryRepository directoryRepository;
    private final MinioService minioService;
    private final StorageMapper storageMapper;

    /**
     * Загружает файл в указанную директорию.
     *
     * @param fileToUpload Файл для загрузки.
     * @param directoryId ID директории, в которую загружается файл.
     * @return DTO загруженного файла.
     * @throws ResourceNotFoundException если директория не найдена.
     * @see FileEntity
     * @see DirectoryEntity
     */
    @Transactional
    public FileDto upload(MultipartFile fileToUpload, Long directoryId) {
        DirectoryEntity directoryEntity = directoryRepository.findById(directoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", directoryId)));

        if (fileToUpload.getOriginalFilename() == null) {
            throw new ResourceNotFoundException("FileEntity not uploaded");
        }
        List<String> fileNameAndExtension = FileUtil.separateFileName(fileToUpload.getOriginalFilename());

        FileEntity fileEntity = FileEntity.builder()
                .directory(directoryEntity)
                .filename(fileNameAndExtension.getFirst())
                .extension(fileNameAndExtension.get(1))
                .build();

        //TODO checksum
        fileEntity = fileRepository.save(fileEntity);

        minioService.upload(fileEntity.getId().toString(), fileToUpload);

        return storageMapper.fileToDto(fileEntity);
    }

    /**
     * Удаляет файл из хранилища.
     *
     * @param fileId ID удаляемого файла.
     * @throws ResourceNotFoundException если файл не найден.
     * @see MinioService#deleteFile(String)
     */
    public void deleteFileById(UUID fileId) {
        if (fileRepository.existsById(fileId)) {
            minioService.deleteFile(fileId.toString());
            fileRepository.deleteById(fileId);
        }
        else {
            throw new ResourceNotFoundException(String.format("FileEntity with id '%s' not found", fileId));
        }
    }

    /**
     * Скачивает файл по его ID.
     *
     * @param fileId ID файла.
     * @return Поток данных загруженного файла.
     * @see MinioService#download(String)
     */
    public InputStream downloadFile(UUID fileId) {
        return minioService.download(fileId.toString());
    }



    /**
     * Проверяет, является ли пользователь владельцем файла.
     *
     * @param id ID файла.
     * @return true, если пользователь является владельцем, иначе false.
     * @throws ResourceNotFoundException если файл не найден.
     */
    @Transactional
    public boolean isFileOwner(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("FileEntity with id %s not exists", id)))
                .getDirectory().getOwner().getUsername().equals(this.getUserName());
    }

    /**
     * Перемещает файл в указанную директорию.
     *
     * @param moveFileDto DTO с информацией о перемещении файла.
     * @return DTO обновленного файла.
     * @throws ResourceNotFoundException если файл или директория не найдены.
     * @see MoveFileDto
     * @see FileEntity
     */
    @Transactional
    public FileDto moveFileToDir(MoveFileDto moveFileDto) {
        DirectoryEntity directoryEntity = directoryRepository.findById(moveFileDto.getTargetDirectoryId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", moveFileDto.getTargetDirectoryId())));
        FileEntity fileEntity = fileRepository.findById(moveFileDto.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("FileEntity with id '%s' not found", moveFileDto.getFileId())));
        fileEntity.setDirectory(directoryEntity);
        fileEntity = fileRepository.save(fileEntity);
        return storageMapper.fileToDto(fileEntity);
    }

    /**
     * Получает имя файла по его ID.
     *
     * @param id ID файла.
     * @return Полное имя файла (имя и расширение).
     * @throws ResourceNotFoundException если файл не найден.
     * @see FileEntity
     */
    public String getFileNameById(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("fileEntity with id %s not exist", id)));
        return fileEntity.getFilename() + '.' + fileEntity.getExtension();
    }
}
