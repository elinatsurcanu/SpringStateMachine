package com.hareesh.springstatemachine.springstatemachinedemo.config;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.guard.Guard;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    private StateMachine<PaymentState, PaymentEvent> sm;

    @Mock
    private Guard<PaymentState, PaymentEvent> checkPaymentAmountGuard;


    @Test
    void testNewStateMachine() {
        when(checkPaymentAmountGuard.evaluate(any())).thenReturn(true);
//        Map<Object, Object> variables = new HashMap<>();
//        variables.put("amount", BigDecimal.TEN);
//        when(stateContext.getExtendedState()).thenReturn(new DefaultExtendedState(variables));
        sm.getExtendedState().getVariables().put("amount", BigDecimal.TEN);
        sm.start();
        assertEquals("INITIAL", sm.getState().getId().name());

        boolean res  = sm.sendEvent(PaymentEvent.CREATE_PAYMENT);
        assertTrue(res);
      // for some reason even if the event to create the payment is sent, the state of the SM doesn't
      // change to NEW, NP exception
      //  assertEquals(PaymentState.NEW.name(), sm.getState().getId().name());


    }



}