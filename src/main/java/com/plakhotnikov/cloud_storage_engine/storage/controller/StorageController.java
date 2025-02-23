package com.plakhotnikov.cloud_storage_engine.storage.controller;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.*;
import com.plakhotnikov.cloud_storage_engine.storage.repository.FileRepository;
import com.plakhotnikov.cloud_storage_engine.storage.service.DirectoryService;
import com.plakhotnikov.cloud_storage_engine.storage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;



@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class StorageController {
    private final FileService fileService;
    private final DirectoryService directoryService;
    private final FileRepository fileRepository;

    @PostMapping("/upload")
    @PreAuthorize("@directoryService.isUserOwner(#directoryId)")
    public FileDto uploadFile(@RequestParam Long directoryId, @RequestParam MultipartFile file) {
        return fileService.upload(file, directoryId);
    }

    @GetMapping("/list")
    @PreAuthorize("@directoryService.isUserOwner(#directoryId)")
    public DirectoryDto list(@RequestParam(required = false, defaultValue = "0") Long directoryId) {
        return directoryService.getDirectory(directoryId);
    }

    @PostMapping("/create-directory")
    @PreAuthorize("#createDirectoryDto.parentDirectoryId == null || @directoryService.isUserOwner(#createDirectoryDto.parentDirectoryId)")
    public DirectoryDto createDirectory(@RequestBody CreateDirectoryDto createDirectoryDto) {
        return directoryService.createDirectory(createDirectoryDto);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        String filename = fileService.getName(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        try (var stream = fileService.downloadFile(fileId)) {
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream.readAllBytes());
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("@fileService.isUserOwner(#fileId)")
    public void deleteFile(@PathVariable UUID fileId) {
        fileRepository.deleteById(fileId);
    }

    @PostMapping("/move")
    @PreAuthorize("@fileService.isUserOwner(#moveFileDto.fileId) && @directoryService.isUserOwner(#moveFileDto.targetDirectoryId)")
    public FileDto moveFile(MoveFileDto moveFileDto) {
        return fileService.move(moveFileDto);
    }
}
