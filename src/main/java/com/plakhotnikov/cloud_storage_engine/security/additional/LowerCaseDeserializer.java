package com.plakhotnikov.cloud_storage_engine.security.additional;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

//todo move to jacson config
public class LowerCaseDeserializer extends JsonDeserializer<String> {

    /**
     * @param p    Parser used for reading JSON content //todo naming
     * @param ctxt Context that can be used to access information about //todo naming
     *             this deserialization activity.
     * @return result in lower case
     * @throws IOException
     * @throws JacksonException
     */
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return value != null ? value.toLowerCase() : null;
    }
}
