package com.plakhotnikov.cloud_storage_engine.storage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
@AllArgsConstructor
@Table(name = "directory")
public class DirectoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_directory_id", referencedColumnName = "id")
    private DirectoryEntity rootDirectory;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity owner;


    @OneToMany(mappedBy = "rootDirectory", fetch = FetchType.LAZY)
    private List<DirectoryEntity> children;


    @OneToMany(mappedBy = "directory", fetch = FetchType.LAZY)
    private List<FileEntity> files;
}
