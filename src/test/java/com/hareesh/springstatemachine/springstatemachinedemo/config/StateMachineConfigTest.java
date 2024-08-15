package com.hareesh.springstatemachine.springstatemachinedemo.config;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());

        sm.start();
        System.out.println("Current SM state: " + sm.getState().toString());
        sm.sendEvent(PaymentEvent.CREATE_PAYMENT);
        System.out.println("State after creating payment: " + sm.getState().toString());
        sm.sendEvent(PaymentEvent.SUBTRACT_MONEY);
        System.out.println("State after subtracting money: " + sm.getState().toString());
        sm.sendEvent(PaymentEvent.DECLINE_PAYMENT);
        System.out.println(sm.getState().toString());

    }



}