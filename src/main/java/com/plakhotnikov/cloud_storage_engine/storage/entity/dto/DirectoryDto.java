package com.plakhotnikov.cloud_storage_engine.storage.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Setter
@Getter
@NoArgsConstructor
public class DirectoryDto {
    private Long id;

    private String name;


    private List<SubDirectoryDto> children;


    private List<FileDto> files;
}
