package com.plakhotnikov.cloud_storage_engine.storage.entity;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.CreateDirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.DirectoryDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.FileDto;
import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.SubDirectoryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorageMapper {

    Directory CreateDirToEntity(CreateDirectoryDto createDirectoryDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    FileDto fileToDto(File file);

//    @Mapping(target = "children", ignore = true) // Отключаем рекурсию
//    @Mapping(target = "files", ignore = true) // Отключаем рекурсию
    @Mapping(target = "children", source = "children", qualifiedByName = "mapSubDir")
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
