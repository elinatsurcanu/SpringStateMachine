package com.hareesh.springstatemachine.springstatemachinedemo.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Product;

import java.io.IOException;
import java.util.HashMap;

@Converter(autoApply = true) // This will apply the converter globally
public class HashMapConverter implements AttributeConverter<HashMap<String, Product>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(HashMap<String, Product> products) {
        if (products == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(products); // Convert HashMap to JSON string
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting HashMap to JSON", e);
        }
    }

    @Override
    public HashMap<String, Product> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Product.class));
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to HashMap", e);
        }
    }
}
