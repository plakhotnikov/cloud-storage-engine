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

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    @Value("#{minioProperties.getBucketName()}")
    private String bucketName;

    @PostConstruct
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
        catch (MinioException e) {
            throw new DownloadException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
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
        catch (MinioException e) {
            throw new DeleteFileException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void upload(String objectName, InputStream inputStream) {
        try {
             minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)

                            .build()
            );
        }
        catch (MinioException e) {
            throw new UploadFileException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
