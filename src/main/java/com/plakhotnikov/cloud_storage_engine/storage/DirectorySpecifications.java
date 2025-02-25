package com.plakhotnikov.cloud_storage_engine.storage;

import com.plakhotnikov.cloud_storage_engine.storage.entity.DirectoryEntity;
import org.springframework.data.jpa.domain.Specification;

public class DirectorySpecifications {
    public static Specification<DirectoryEntity> hasOwnerEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("owner").get("email"), email);
    }

    public static Specification<DirectoryEntity> isRootDirectory() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("rootDirectory"));
    }
}
