package com.hareesh.springstatemachine.springstatemachinedemo.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExceptionHandler {

    public static ResponseEntity<HashMap<String, String>> handleException(String errorMsg) {
        HashMap<String, String> errorMap = new HashMap<>();
        errorMap.put("error", errorMsg);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
    }
}
