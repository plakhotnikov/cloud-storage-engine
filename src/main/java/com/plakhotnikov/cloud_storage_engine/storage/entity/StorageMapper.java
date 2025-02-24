package com.plakhotnikov.cloud_storage_engine.storage.entity;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorageMapper {

    FileDto fileToDto(FileEntity fileEntity);

    @Mapping(source = "children", target = "children", qualifiedByName = "mapSubDir")
    @Mapping(source = "files", target = "files", qualifiedByName = "mapFile")
    DirectoryDto dirToDto(DirectoryEntity directoryEntity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    SubDirectoryDto dirToSubDto(DirectoryEntity directoryEntity);



    @Named("mapSubDir")
    default List<SubDirectoryDto> mapSubDir(List<DirectoryEntity> directories) {
        return directories.stream()
                .map(this::dirToSubDto)
                .toList();
    }

    @Named("mapFile")
    default List<FileDto> mapFile(List<FileEntity> fileEntities) {
        return fileEntities.stream()
                .map(this::fileToDto)
                .toList();
    }



}
