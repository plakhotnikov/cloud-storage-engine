package com.plakhotnikov.cloud_storage_engine.exception;

public class DeleteFileException extends RuntimeException{
    public DeleteFileException() {
    }

    public DeleteFileException(String message) {
        super(message);
    }

    public DeleteFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteFileException(Throwable cause) {
        super(cause);
    }
}
