package com.plakhotnikov.cloud_storage_engine.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Кастомный десериализатор JSON-строк, преобразующий входные данные в нижний регистр.
 * Используется для автоматического преобразования строк в lowercase при десериализации.
 *
 * @see JsonDeserializer
 */
public class LowerCaseDeserializer extends JsonDeserializer<String> {

    /**
     * Десериализует строковое значение из JSON и преобразует его в нижний регистр.
     *
     * @param p    Парсер JSON, используемый для чтения содержимого.
     * @param ctxt Контекст десериализации, предоставляющий информацию о процессе.
     * @return Строка в нижнем регистре или null, если входное значение отсутствует.
     * @throws IOException В случае ошибки при чтении JSON.
     */
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
        String value = p.getText();
        return value != null ? value.toLowerCase() : null;
    }
}
