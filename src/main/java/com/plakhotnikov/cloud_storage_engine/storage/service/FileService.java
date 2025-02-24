package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.FileEntity;
import com.plakhotnikov.cloud_storage_engine.storage.entity.StorageMapper;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.FileDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.MoveFileDto;
import com.plakhotnikov.cloud_storage_engine.storage.repository.DirectoryRepository;
import com.plakhotnikov.cloud_storage_engine.storage.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final DirectoryRepository directoryRepository;
    private final MinioService minioService;
    private final StorageMapper storageMapper;

    @Transactional
    public FileDto upload(MultipartFile fileToUpload, Long directoryId) {
        DirectoryEntity directoryEntity = directoryRepository.findById(directoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", directoryId)));

        if (fileToUpload.getOriginalFilename() == null) {
            throw new ResourceNotFoundException("FileEntity not uploaded");
        }
        List<String> fileNameAndExtension = separateFileName(fileToUpload.getOriginalFilename());

        FileEntity fileEntity = FileEntity.builder()
                .directoryEntity(directoryEntity)
                .filename(fileNameAndExtension.getFirst())
                .extension(fileNameAndExtension.get(1))
                .build();

        //TODO checksum
        fileEntity = fileRepository.save(fileEntity);

        minioService.upload(fileEntity.getId().toString(), fileToUpload);

        return storageMapper.fileToDto(fileEntity);
    }

    public void deleteFile(UUID fileId) {
        if (fileRepository.existsById(fileId)) {
            minioService.deleteFile(fileId.toString());
            fileRepository.deleteById(fileId);
        }
        else {
            throw new ResourceNotFoundException(String.format("FileEntity with id '%s' not found", fileId));
        }
    }

    public InputStream downloadFile(UUID fileId) {
        return minioService.download(fileId.toString());
    }

    private List<String> separateFileName(String fileName) {
        int i = fileName.lastIndexOf('.');
        return List.of(fileName.substring(0, i), fileName.substring(i + 1));
    }


    @Transactional
    public boolean isFileOwner(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("FileEntity with id %s not exists", id)))
                .getDirectoryEntity().getOwner().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Transactional
    public FileDto moveFileToDir(MoveFileDto moveFileDto) {
        DirectoryEntity directoryEntity = directoryRepository.findById(moveFileDto.getTargetDirectoryId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("DirectoryEntity with id '%s' not found", moveFileDto.getTargetDirectoryId())));
        FileEntity fileEntity = fileRepository.findById(moveFileDto.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("FileEntity with id '%s' not found", moveFileDto.getFileId())));
        fileEntity.setDirectoryEntity(directoryEntity);
        fileEntity = fileRepository.save(fileEntity);
        return storageMapper.fileToDto(fileEntity);
    }

    public String getFileNameById(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("fileEntity with id %s not exist", id)));
        return fileEntity.getFilename() + '.' + fileEntity.getExtension();
    }
}
