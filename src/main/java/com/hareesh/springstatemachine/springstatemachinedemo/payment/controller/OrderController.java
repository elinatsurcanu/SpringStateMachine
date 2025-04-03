package com.hareesh.springstatemachine.springstatemachinedemo.payment.controller;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.CustomerOrder;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.PaymentException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.services.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.ExceptionHandler.handleInsufficientFundsException;
import static com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.ExceptionHandler.handlePaymentException;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @GetMapping(value = "/order/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerOrder> getOrderById(@PathVariable(value = "id") Long orderId)  {
        return ResponseEntity.ok()
                .body(orderService.getOrderById(orderId));
    }

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerOrder>> getAllOrders() {
        return ResponseEntity.ok()
                .body(orderService.getAllOrders());
    }

    @GetMapping(value = "/currentBalance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, BigDecimal>> getAvailableFunds() {
        final HashMap<String, BigDecimal> availableFunds = new HashMap<>();
        availableFunds.put("availableFunds", Account.accountBalance);
        availableFunds.put("limitPerPayment", BigDecimal.valueOf(500L));

        return ResponseEntity.ok()
                .body(availableFunds);
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewOrder(@RequestBody CustomerOrder customerOrder) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(orderService.createNewOrder(customerOrder));
        } catch (InsufficientFundsException e) {
            return handleInsufficientFundsException(e.getMessage());
        }
    }

    @PostMapping(value = "/process/payment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processPayment(@PathVariable(value = "id") Long paymentId) {
        try {
            orderService.processOrder(paymentId);
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
