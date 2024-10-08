package com.hareesh.springstatemachine.springstatemachinedemo.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.repository.PaymentRepository;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


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