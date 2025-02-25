package com.plakhotnikov.cloud_storage_engine.storage.entity;

import com.plakhotnikov.cloud_storage_engine.storage.entity.dto.*;
import org.mapstruct.*;

import java.util.List;

/**
 * Маппер для преобразования сущностей хранения в DTO-объекты.
 *
 * @see FileEntity
 * @see DirectoryEntity
 * @see FileDto
 * @see DirectoryDto
 * @see SubDirectoryDto
 */
@Mapper(componentModel = "spring")
public interface StorageMapper {

    /**
     * Преобразует сущность файла в DTO.
     *
     * @param fileEntity Сущность файла.
     * @return DTO файла.
     */
    FileDto fileToDto(FileEntity fileEntity);

    /**
     * Преобразует сущность директории в DTO.
     *
     * @param directoryEntity Сущность директории.
     * @return DTO директории.
     */
    @Mapping(source = "children", target = "children", qualifiedByName = "mapSubDir")
    @Mapping(source = "files", target = "files", qualifiedByName = "mapFile")
    DirectoryDto dirToDto(DirectoryEntity directoryEntity);

    /**
     * Преобразует сущность директории в DTO поддиректории
     *
     * @param directoryEntity Сущность директории.
     * @return DTO поддиректории.
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    SubDirectoryDto dirToSubDto(DirectoryEntity directoryEntity);


    /**
     * Преобразует список сущностей директории в список DTO поддиректорий.
     *
     * @param directories Список сущностей директорий.
     * @return Список DTO поддиректорий.
     */
    @Named("mapSubDir")
    default List<SubDirectoryDto> mapSubDir(List<DirectoryEntity> directories) {
        return directories.stream()
                .map(this::dirToSubDto)
                .toList();
    }

    /**
     * Преобразует список сущностей файлов в список DTO файлов.
     *
     * @param fileEntities Список сущностей файлов.
     * @return Список DTO файлов.
     */
    @Named("mapFile")
    default List<FileDto> mapFile(List<FileEntity> fileEntities) {
        return fileEntities.stream()
                .map(this::fileToDto)
                .toList();
    }



}
