package com.intuit.data.simplan.global.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 28-Feb-2022 at 11:18 PM
 */
public class JacksonUtil {
    public static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    }

    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T fromJson(String jsonData, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(jsonData, t);
    }

    public static Map<String, Object> fromJsonToMap(String jsonData) throws JsonProcessingException {
        return objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
        });

    }

    public static <T> T fromJsonWithTypeReference(String jsonData, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(jsonData, new TypeReference<T>() {
        });

    }


}