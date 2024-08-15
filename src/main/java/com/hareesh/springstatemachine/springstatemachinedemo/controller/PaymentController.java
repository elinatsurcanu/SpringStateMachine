package com.hareesh.springstatemachine.springstatemachinedemo.controller;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Payment> createNewPayment(@RequestBody Payment payment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createNewPayment(payment.getAmount()));
    }

    @PostMapping(value = "/process/payment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Payment> processPayment(@PathVariable(value = "id") Long paymentId) {
        try {
            paymentService.processPayment(paymentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


}
