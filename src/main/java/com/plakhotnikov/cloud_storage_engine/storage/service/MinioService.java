package com.plakhotnikov.cloud_storage_engine.storage.service;

import com.plakhotnikov.cloud_storage_engine.exception.DeleteFileException;
import com.plakhotnikov.cloud_storage_engine.exception.DownloadException;
import com.plakhotnikov.cloud_storage_engine.exception.UploadFileException;
import com.plakhotnikov.cloud_storage_engine.properties.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    private final MinioProperties minioProperties;


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
}
