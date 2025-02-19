package com.plakhotnikov.cloud_storage_engine.storage.entity.dto;

import com.plakhotnikov.cloud_storage_engine.storage.entity.Directory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class UserFilesDto {
    String username;
    List<Directory> files;
}
