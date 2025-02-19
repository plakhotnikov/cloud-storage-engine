package com.plakhotnikov.cloud_storage_engine.storage.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class FileDto {
    private UUID id;
    private String filename;
    private String extension;
}
