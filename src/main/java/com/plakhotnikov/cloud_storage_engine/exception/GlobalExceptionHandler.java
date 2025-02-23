package com.plakhotnikov.cloud_storage_engine.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleResourceNotFoundException(ResourceNotFoundException e) {
        return new ExceptionResponse(e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(exception = {AccessDeniedException.class, JwtException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleForbiddenExceptions(RuntimeException e) {
        return new ExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(exception = {DeleteFileException.class, DownloadException.class, UploadFileException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleInternalServerExceptions(AccessDeniedException e) {
        return new ExceptionResponse(e.getMessage(), HttpStatus.FORBIDDEN.value());
    }
}
