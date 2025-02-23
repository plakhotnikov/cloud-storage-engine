package com.plakhotnikov.cloud_storage_engine.storage.entity;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorageMapper {

    FileDto fileToDto(File file);

    @Mapping(source = "children", target = "children", qualifiedByName = "mapSubDir")
    @Mapping(source = "files", target = "files", qualifiedByName = "mapFile")
    DirectoryDto dirToDto(Directory directory);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    SubDirectoryDto dirToSubDto(Directory directory);



    @Named("mapSubDir")
    default List<SubDirectoryDto> mapSubDir(List<Directory> directories) {
        return directories.stream()
                .map(this::dirToSubDto)
                .toList();
    }

    @Named("mapFile")
    default List<FileDto> mapFile(List<File> files) {
        return files.stream()
                .map(this::fileToDto)
                .toList();
    }



}
