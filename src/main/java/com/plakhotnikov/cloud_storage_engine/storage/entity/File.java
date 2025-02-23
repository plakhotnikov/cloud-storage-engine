package com.plakhotnikov.cloud_storage_engine.storage.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plakhotnikov.cloud_storage_engine.security.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@ToString
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String filename;

    private String extension;

    private String checkSum;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id", referencedColumnName = "id")
    private Directory directory;
}
