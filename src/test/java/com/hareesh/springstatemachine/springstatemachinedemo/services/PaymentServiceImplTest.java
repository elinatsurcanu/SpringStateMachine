package com.hareesh.springstatemachine.springstatemachinedemo.services;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;


@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;


//    @Transactional
//    @Test
//    void preAuth() {
//        Payment savedPayment = paymentService.createNewPayment(new BigDecimal("15.75"));
//        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId(), new BigDecimal("15.75"));
//        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());
//        System.out.println(sm.getState().getId());
//        System.out.println(preAuthPayment);
//    }
}