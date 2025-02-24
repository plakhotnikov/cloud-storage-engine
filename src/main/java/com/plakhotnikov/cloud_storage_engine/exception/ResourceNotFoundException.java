package com.plakhotnikov.cloud_storage_engine.exception;

public class ResourceNotFoundException extends RuntimeException {


    public ResourceNotFoundException(String message) {
        super(message);
    }
}
