package com.plakhotnikov.cloud_storage_engine;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Тестовый контроллер для проверки доступности API.
 */
@RestController
@RequestMapping("/hello")
public class TestController {
    /**
     * Обрабатывает GET-запрос по корневому пути "/hello/".
     * @return Ответ с HTTP статусом 200 OK и строкой "Hello World".
     */
    @GetMapping("/")
    @Operation(summary = "Приветственное сообщение", description = "Возвращает строку \"Hello World\" для проверки работы API")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }
}
