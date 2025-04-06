package com.springstatemachine.springstatemachinedemo.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springstatemachine.springstatemachinedemo.order.domain.Item;

import java.io.IOException;
import java.util.List;

@Converter
public class ItemListConverter implements AttributeConverter<List<Item>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Item> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ItemList to JSON", e);
        }
    }

    @Override
    public List<Item> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to ItemList", e);
        }
    }
}
