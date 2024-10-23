package com.hareesh.springstatemachine.springstatemachinedemo.config;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.guard.Guard;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> smFactory;

    @Mock
    private Guard<PaymentState, PaymentEvent> checkPaymentAmountGuard;

    @Test
    void testStateMachinePositiveFlow() {
        StateMachine<PaymentState, PaymentEvent> sm = smFactory.getStateMachine();

        sm.getExtendedState().getVariables().put("amount", BigDecimal.TEN);
        when(checkPaymentAmountGuard.evaluate(any())).thenReturn(true);

        sm.start();

        assertEquals(PaymentState.INITIAL.name(), sm.getState().getId().name());

        sm.sendEvent(PaymentEvent.CREATE_PAYMENT);
        assertEquals(PaymentState.NEW.name(), sm.getState().getId().name());

        sm.sendEvent(PaymentEvent.SUBTRACT_MONEY);
        assertEquals(PaymentState.SUCCESS.name(), sm.getState().getId().name());
    }

}