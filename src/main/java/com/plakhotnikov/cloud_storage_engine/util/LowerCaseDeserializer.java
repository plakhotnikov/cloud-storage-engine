package com.plakhotnikov.cloud_storage_engine.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class LowerCaseDeserializer extends JsonDeserializer<String> {
    /**
     * @param p    Parser used for reading JSON content
     * @param ctxt Context that can be used to access information about
     *             this deserialization activity.
     * @return result in lower case
     * @throws {@link IOException}
     */
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
        String value = p.getText();
        return value != null ? value.toLowerCase() : null;
    }
}
