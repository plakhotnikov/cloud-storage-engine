package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.exception.DeleteFileException;
import com.plakhotnikov.cloud_storage_engine.exception.DownloadException;
import com.plakhotnikov.cloud_storage_engine.exception.UploadFileException;
import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    @Value("#{minioProperties.getBucketName()}")
    private String bucketName;

    @PostConstruct // todo вынести в конфиг
    public void init() {
        try {
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            )) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        }
        catch (Exception e) {
            throw new DownloadException(e.getMessage());
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        }
        catch (Exception e) {
            throw new DeleteFileException(e.getMessage());
        }
    }

    public void upload(String objectName, MultipartFile file) {
        try(InputStream inputStream = file.getInputStream()) {
             minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)

                            .build()
            );
        }
        catch (Exception e) {
            throw new UploadFileException(e.getMessage());
        }
    }
}
