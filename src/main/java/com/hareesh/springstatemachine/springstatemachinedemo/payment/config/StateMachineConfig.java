package com.hareesh.springstatemachine.springstatemachinedemo.payment.config;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.OrderEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.OrderState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.math.BigDecimal;
import java.util.EnumSet;

@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

    static final Logger LOGGER = LoggerFactory.getLogger(StateMachineConfig.class);

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {

        states.withStates()
                .initial(OrderState.INITIAL)
                .states(EnumSet.allOf(OrderState.class))
                .end(OrderState.CANCELLED)
                .end(OrderState.PROCESSED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(OrderState.INITIAL).target(OrderState.NEW).event(OrderEvent.CREATE_ORDER)
                .guard(checkPaymentAmountGuard())
                .and()

                .withExternal()
                .source(OrderState.INITIAL).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
                .and()

                .withExternal()
                .source(OrderState.NEW).target(OrderState.PROCESSED).event(OrderEvent.PROCESS_ORDER)
                .action(processOrderAction())
                .and()

                .withExternal()
                .source(OrderState.NEW).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
                .action(processOrderAction());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        StateMachineListenerAdapter<OrderState, OrderEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
                if (from != null) {
                    LOGGER.info("State Changed from : {}, to: {}", from.getIds(), to.getIds());
                }
            }
        };

        config
                .withConfiguration()
                .listener(adapter);
    }

    public Guard<OrderState, OrderEvent> checkPaymentAmountGuard() {
        return context -> {
            BigDecimal amount = context.getExtendedState().get("amount", BigDecimal.class);

            LOGGER.info("Checking the payment amount...");

            return amount != null && amount.compareTo(Account.limitPerPayment) <= 0;

        };
    }

    public Action<OrderState, OrderEvent> processOrderAction() {
        return context -> {
            BigDecimal amount = context.getExtendedState().get("amount", BigDecimal.class);
            Long paymentId = context.getExtendedState().get("paymentId", Long.class);
            LOGGER.info("Process payment action called for payment with id = {} and amount = {}", paymentId, amount);
        };
    }
}
