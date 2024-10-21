package com.hareesh.springstatemachine.springstatemachinedemo.payment.controller;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.PaymentException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.ExceptionHandler.handleInsufficientFundsException;
import static com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.ExceptionHandler.handlePaymentException;

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

    @GetMapping(value = "/currentBalance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, BigDecimal>> getAvailableFunds() {
        final HashMap<String, BigDecimal> availableFunds = new HashMap<>();
        availableFunds.put("availableFunds", Account.accountBalance);
        availableFunds.put("limitPerPayment", BigDecimal.valueOf(500L));

        return ResponseEntity.ok()
                .body(availableFunds);
    }

    @PostMapping(value = "/payment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewPayment(@RequestBody Payment payment) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(paymentService.createNewPayment(payment.getAmount()));
        } catch (InsufficientFundsException e) {
            return handleInsufficientFundsException(e.getMessage());
        }
    }

    @PostMapping(value = "/process/payment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processPayment(@PathVariable(value = "id") Long paymentId) {
        try {
            paymentService.processPayment(paymentId);
        } catch (InsufficientFundsException e) {
            return handleInsufficientFundsException(e.getMessage());
        } catch (PaymentException e) {
            return handlePaymentException(e.getMessage());
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("currentBalance", Account.accountBalance);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
