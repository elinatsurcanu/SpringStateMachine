package com.hareesh.springstatemachine.springstatemachinedemo.services;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

   void preAuth(Long paymentId, BigDecimal amount, StateMachine<PaymentState, PaymentEvent> sm);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);

    Payment getPaymentById(Long paymentId);

    Payment createNewPayment(BigDecimal amount);

    Payment processPayment(Long paymentId);

    public List<Payment> getAllPayments();

}
