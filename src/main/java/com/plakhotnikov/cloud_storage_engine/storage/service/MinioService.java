package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.exception.DeleteFileException;
import com.plakhotnikov.cloud_storage_engine.exception.DownloadException;
import com.plakhotnikov.cloud_storage_engine.exception.UploadFileException;
import com.plakhotnikov.cloud_storage_engine.properties.MinioProperties;
import io.minio.*;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;


/**
 * Сервис для работы с MinIO, включающий загрузку, скачивание и удаление файлов.
 *
 * @see MinioClient
 * @see MinioProperties
 */
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    /**
     * Скачивает файл из MinIO.
     *
     * @param objectName Имя объекта в хранилище.
     * @return Поток данных загруженного файла.
     * @throws DownloadException Если произошла ошибка при скачивании файла.
     * @see DownloadException
     * @see GetObjectArgs
     */
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
        }
        catch (Exception e) {
            throw new DownloadException(e.getMessage());
        }
    }

    /**
     * Удаляет файл из MinIO.
     *
     * @param objectName Имя объекта в хранилище.
     * @throws DeleteFileException Если произошла ошибка при удалении файла.
     * @see DeleteFileException
     * @see RemoveObjectArgs
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
        }
        catch (Exception e) {
            throw new DeleteFileException(e.getMessage());
        }
    }

    /**
     * Загружает файл в MinIO.
     *
     * @param objectName Имя объекта в хранилище.
     * @param file Файл для загрузки.
     * @throws UploadFileException Если произошла ошибка при загрузке файла.
     * @see UploadFileException
     * @see PutObjectArgs
     */
    public void upload(String objectName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
             minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)

                            .build()
            );
        }
        catch (Exception e) {
            throw new UploadFileException(e.getMessage());
        }
    }

    /**
     * Удаляет список объектов из MinIO-хранилища.
     *
     * <p>Метод принимает список имен объектов и удаляет их из указанного бакета в MinIO.
     * В случае ошибки выбрасывается {@link DeleteFileException}.</p>
     *
     * @param objectNames список имен объектов, которые необходимо удалить
     * @throws DeleteFileException если произошла ошибка при удалении объектов
     */
    public void deleteAll(List<String> objectNames) {
        try {
            List<DeleteObject> objectsToDelete = objectNames.stream()
                    .map(DeleteObject::new)
                    .toList();
            minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .objects(objectsToDelete)
                            .build()
            );
        }
        catch (Exception e) {
            throw new DeleteFileException(e.getMessage());
        }
    }
}
