package com.hareesh.springstatemachine.springstatemachinedemo.payment.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.PaymentException;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

   void preAuth(Long paymentId, BigDecimal amount, StateMachine<PaymentState, PaymentEvent> sm) throws InsufficientFundsException;

    Payment getPaymentById(Long paymentId);

    Payment createNewPayment(BigDecimal amount) throws InsufficientFundsException;

    Payment processPayment(Long paymentId) throws InsufficientFundsException, PaymentException;

    List<Payment> getAllPayments();

}
