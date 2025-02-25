package com.plakhotnikov.cloud_storage_engine.storage.controller;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.*;
import com.plakhotnikov.cloud_storage_engine.storage.repository.FileRepository;
import com.plakhotnikov.cloud_storage_engine.storage.service.DirectoryService;
import com.plakhotnikov.cloud_storage_engine.storage.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
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


/**
 * Контроллер для управления файлами и директориями в облачном хранилище.
 *
 * @see FileService
 * @see DirectoryService
 * @see FileRepository
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class StorageController {
    private final FileService fileService;
    private final DirectoryService directoryService;

    /**
     * Загружает файл в указанную директорию.
     *
     * @param directoryId ID директории.
     * @param file Файл для загрузки.
     * @return DTO загруженного файла.
     */
    @PostMapping("/upload")
    @PreAuthorize("@directoryService.isDirectoryOwner(#directoryId)")
    @Operation(summary = "Загрузка файла", description = "Загружает файл в указанную директорию")
    public FileDto uploadFile(@RequestParam Long directoryId, @RequestParam MultipartFile file) {
        return fileService.upload(file, directoryId);
    }

    /**
     * Получает список файлов и поддиректорий в указанной директории.
     *
     * @param directoryId ID директории.
     * @return DTO директории с файлами и поддиректориями.
     */
    @GetMapping("/list")
    @PreAuthorize("@directoryService.isDirectoryOwner(#directoryId)")
    @Operation(summary = "Получение списка файлов", description = "Возвращает список файлов и поддиректорий в указанной директории")
    public DirectoryDto list(@RequestParam(required = false, defaultValue = "0") Long directoryId) {
        return directoryService.getDirectoryById(directoryId);
    }


    /**
     * Создаёт новую директорию.
     *
     * @param createDirectoryDto DTO с данными для создания директории.
     * @return DTO созданной директории.
     */
    @PostMapping("/create-directory")
    @PreAuthorize("#createDirectoryDto.parentDirectoryId == null || @directoryService.isDirectoryOwner(#createDirectoryDto.parentDirectoryId)")
    @Operation(summary = "Создание директории", description = "Создаёт новую директорию в указанном расположении")
    public DirectoryDto createDirectory(@RequestBody CreateDirectoryDto createDirectoryDto) {
        return directoryService.createDirectory(createDirectoryDto);
    }


    /**
     * Скачивает файл по его ID.
     *
     * @param fileId ID файла.
     * @return Ответ с байтовым содержимым файла.
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "Скачивание файла", description = "Скачивает файл по его ID")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        String filename = fileService.getFileNameById(fileId);
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


    /**
     * Удаляет файл по его ID.
     *
     * @param fileId ID файла.
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("@fileService.isFileOwner(#fileId)")
    @Operation(summary = "Удаление файла", description = "Удаляет файл по его ID")

    public void deleteFile(@PathVariable UUID fileId) {
        fileService.deleteFileById(fileId);
    }


    /**
     * Перемещает файл в другую директорию.
     *
     * @param moveFileDto DTO с информацией о перемещении файла.
     * @return DTO обновленного файла.
     */
    @PostMapping("/move")
    @PreAuthorize("@fileService.isFileOwner(#moveFileDto.fileId) && @directoryService.isDirectoryOwner(#moveFileDto.targetDirectoryId)")
    @Operation(summary = "Перемещение файла", description = "Перемещает файл в другую директорию")
    public FileDto moveFile(MoveFileDto moveFileDto) {
        return fileService.moveFileToDir(moveFileDto);
    }


    /**
     * Удаляет директорию по её идентификатору.
     *
     * <p>Этот метод обрабатывает HTTP POST-запрос по пути {@code /delete-directory}.
     * Перед выполнением операции проверяется, является ли текущий пользователь владельцем директории.
     * В случае успешного удаления возвращается HTTP 200 OK.</p>
     *
     * @param id идентификатор директории, которая должна быть удалена
     * @return {@link ResponseEntity} с кодом состояния HTTP 200 OK в случае успешного удаления
     */
    @PostMapping("/delete-directory")
    @PreAuthorize("@directoryService.isDirectoryOwner(#id)")
    public ResponseEntity<?> deleteDirectory(@RequestBody Long id) {
        directoryService.deleteDirectoryById(id);
        return ResponseEntity.ok().build();
    }
}
