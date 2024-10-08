package com.hareesh.springstatemachine.springstatemachinedemo.payment.controller;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

import static com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.ExceptionHandler.handleException;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @GetMapping(value = "/payment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Payment> getPaymentById(@PathVariable(value = "id") Long paymentId)  {
        return ResponseEntity.ok()
                .body(paymentService.getPaymentById(paymentId));
    }

    @GetMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok()
                .body(paymentService.getAllPayments());
    }

    @PostMapping(value = "/payment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewPayment(@RequestBody Payment payment) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(paymentService.createNewPayment(payment.getAmount()));
        } catch (InsufficientFundsException e) {
            return handleException(e.getMessage());
        }
    }

    @PostMapping(value = "/process/payment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processPayment(@PathVariable(value = "id") Long paymentId) {
        try {
            paymentService.processPayment(paymentId);
        } catch (Exception e) {
            return handleException(e.getMessage());
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("currentBalance", Account.accountBalance);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
