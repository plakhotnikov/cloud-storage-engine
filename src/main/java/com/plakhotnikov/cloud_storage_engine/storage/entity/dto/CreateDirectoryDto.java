package com.plakhotnikov.cloud_storage_engine.storage.entity.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateDirectoryDto {
    private Long parentDirectoryId;
    private String name;
}
