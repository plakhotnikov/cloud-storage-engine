package com.plakhotnikov.cloud_storage_engine.storage.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Setter
@Getter
public class MoveFileDto {
    private UUID fileId;
    private Long targetDirectoryId;
}