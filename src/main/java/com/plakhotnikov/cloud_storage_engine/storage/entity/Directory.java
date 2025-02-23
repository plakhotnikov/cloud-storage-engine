package com.plakhotnikov.cloud_storage_engine.storage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
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

public class Directory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_directory_id", referencedColumnName = "id")
    private Directory rootDirectory;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;


    @OneToMany(mappedBy = "rootDirectory", fetch = FetchType.LAZY)
    private List<Directory> children;


    @OneToMany(mappedBy = "directory", fetch = FetchType.LAZY)
    private List<File> files;
}
