package com.hareesh.springstatemachine.springstatemachinedemo.payment.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExceptionHandler {

    public static ResponseEntity<HashMap<String, String>> handleInsufficientFundsException(String errorMsg) {
        HashMap<String, String> errorMap = new HashMap<>();
        errorMap.put("error", errorMsg);
        errorMap.put("status", "declined");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
    }

    public static ResponseEntity<HashMap<String, String>> handlePaymentException(String errorMsg) {
        HashMap<String, String> errorMap = new HashMap<>();
        errorMap.put("error", errorMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }
}
