package com.hareesh.springstatemachine.springstatemachinedemo.services;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

import java.util.List;

public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);

    Payment getPaymentById(Long paymentId);

    public List<Payment> getAllPayments();

}
