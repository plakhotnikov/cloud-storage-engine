package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.exception.ResourceNotFoundException;
import com.plakhotnikov.cloud_storage_engine.storage.entity.Directory;
import com.plakhotnikov.cloud_storage_engine.storage.entity.File;
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
        Directory directory = directoryRepository.findById(directoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Directory with id '%s' not found", directoryId)));
        File file = new File();
        file.setDirectory(directory);
        if (fileToUpload.getOriginalFilename() == null) {
            throw new ResourceNotFoundException("File not uploaded");
        }
        List<String> fileNameAndExtension = separateFileName(fileToUpload.getOriginalFilename());

        file.setFilename(fileNameAndExtension.get(0));
        file.setExtension(fileNameAndExtension.get(1));
        //TODO checksum
        file = fileRepository.save(file);
        try {
            minioService.upload(file.getId().toString(), fileToUpload.getInputStream());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return storageMapper.fileToDto(file);
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        if (fileRepository.existsById(fileId)) {
            minioService.deleteFile(fileId.toString());
            fileRepository.deleteById(fileId);
        }
        else {
            throw new ResourceNotFoundException(String.format("File with id '%s' not found", fileId));
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
    public boolean isUserOwner(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("File with id %s not exists", id)))
                .getDirectory().getOwner().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Transactional
    public FileDto move(MoveFileDto moveFileDto) {
        Directory directory = directoryRepository.findById(moveFileDto.getTargetDirectoryId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Directory with id '%s' not found", moveFileDto.getTargetDirectoryId())));
        File file = fileRepository.findById(moveFileDto.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("File with id '%s' not found", moveFileDto.getFileId())));
        file.setDirectory(directory);
        file = fileRepository.save(file);
        return storageMapper.fileToDto(file);
    }

    public String getName(UUID id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("file with id %s not exist", id)));
        return file.getFilename() + '.' + file.getExtension();
    }
}
