package com.hareesh.springstatemachine.springstatemachinedemo.payment.config;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentState;
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
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    static final Logger LOGGER = LoggerFactory.getLogger(StateMachineConfig.class);

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {

        states.withStates()
                .initial(PaymentState.INITIAL)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.DECLINED)
                .end(PaymentState.SUCCESS);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(PaymentState.INITIAL).target(PaymentState.NEW).event(PaymentEvent.CREATE_PAYMENT)
                .guard(checkPaymentAmountGuard())
                .and()

                .withExternal()
                .source(PaymentState.INITIAL).target(PaymentState.DECLINED).event(PaymentEvent.DECLINE_PAYMENT)
                .and()

                .withExternal()
                .source(PaymentState.NEW).target(PaymentState.SUCCESS).event(PaymentEvent.SUBTRACT_MONEY)
                .action(processPaymentAction())
                .and()

                .withExternal()
                .source(PaymentState.NEW).target(PaymentState.DECLINED).event(PaymentEvent.DECLINE_PAYMENT)
                .action(processPaymentAction());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                if (from != null) {
                    LOGGER.info("State Changed from : {}, to: {}", from.getIds(), to.getIds());
                }
            }
        };

        config
                .withConfiguration()
                .listener(adapter);
    }

    public Guard<PaymentState, PaymentEvent> checkPaymentAmountGuard() {
        return context -> {
            BigDecimal amount = context.getExtendedState().get("amount", BigDecimal.class);

            LOGGER.info("Checking the payment amount...");

            return amount != null && amount.compareTo(Account.limitPerPayment) <= 0;

        };
    }

    public Action<PaymentState, PaymentEvent> processPaymentAction() {
        return context -> {
            BigDecimal amount = context.getExtendedState().get("amount", BigDecimal.class);
            Long paymentId = context.getExtendedState().get("paymentId", Long.class);
            LOGGER.info("Process payment action called for payment with id = {} and amount = {}", paymentId, amount);
        };
    }
}
