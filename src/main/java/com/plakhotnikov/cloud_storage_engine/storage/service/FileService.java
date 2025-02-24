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

@Service
@RequiredArgsConstructor
public class FileService extends AbstractSecuredController {
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



    @Transactional
    public boolean isFileOwner(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("FileEntity with id %s not exists", id)))
                .getDirectory().getOwner().getUsername().equals(this.getUserName());
    }

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

    public String getFileNameById(UUID id) {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("fileEntity with id %s not exist", id)));
        return fileEntity.getFilename() + '.' + fileEntity.getExtension();
    }
}
