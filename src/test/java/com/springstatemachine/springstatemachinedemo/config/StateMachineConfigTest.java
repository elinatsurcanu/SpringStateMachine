package com.springstatemachine.springstatemachinedemo.config;

import com.springstatemachine.springstatemachinedemo.order.domain.OrderEvent;
import com.springstatemachine.springstatemachinedemo.order.domain.OrderState;
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
    private StateMachineFactory<OrderState, OrderEvent> smFactory;

    @Mock
    private Guard<OrderState, OrderEvent> checkPaymentAmountGuard;

    @Test
    void testStateMachinePositiveFlow() {
        StateMachine<OrderState, OrderEvent> sm = smFactory.getStateMachine();

        sm.getExtendedState().getVariables().put("amount", BigDecimal.TEN);
        when(checkPaymentAmountGuard.evaluate(any())).thenReturn(true);

        sm.start();

        assertEquals(OrderState.INITIAL.name(), sm.getState().getId().name());

        sm.sendEvent(OrderEvent.CREATE_ORDER);
        assertEquals(OrderState.NEW.name(), sm.getState().getId().name());

        sm.sendEvent(OrderEvent.PROCESS_ORDER);
        assertEquals(OrderState.PROCESSED.name(), sm.getState().getId().name());
    }

}