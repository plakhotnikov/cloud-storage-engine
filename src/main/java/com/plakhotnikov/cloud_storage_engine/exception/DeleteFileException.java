package com.plakhotnikov.cloud_storage_engine.exception;

public class DeleteFileException extends RuntimeException{
    public DeleteFileException(String message) {
        super(message);
    }
}
