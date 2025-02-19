package com.plakhotnikov.cloud_storage_engine.storage.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@NoArgsConstructor
public class UploadFileDto {
    private Long directoryId;
    private MultipartFile file;
}
