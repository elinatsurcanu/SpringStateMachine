package com.hareesh.springstatemachine.springstatemachinedemo.services;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.exception.InsufficientFundsException;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

   void preAuth(Long paymentId, BigDecimal amount, StateMachine<PaymentState, PaymentEvent> sm) throws InsufficientFundsException;

    Payment getPaymentById(Long paymentId);

    Payment createNewPayment(BigDecimal amount) throws InsufficientFundsException;

    Payment processPayment(Long paymentId) throws InsufficientFundsException;

    List<Payment> getAllPayments();

}
