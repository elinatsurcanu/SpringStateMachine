package com.hareesh.springstatemachine.springstatemachinedemo.config;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.math.BigDecimal;
import java.util.EnumSet;

@Slf4j
@EnableStateMachine
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

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
        transitions.withExternal().source(PaymentState.INITIAL).target(PaymentState.NEW).event(PaymentEvent.CREATE_PAYMENT)
                .action(preAuthAction()).guard(paymentIdGuard())
                .and()
                .withExternal().source(PaymentState.INITIAL).target(PaymentState.DECLINED).event(PaymentEvent.DECLINE_PAYMENT)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.SUCCESS).event(PaymentEvent.SUBTRACT_MONEY)
                .action(processPaymentAction()).guard(paymentIdGuard())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.DECLINED).event(PaymentEvent.DECLINE_PAYMENT)
                .action(processPaymentAction());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State Changed from : {}, to: {}", from.getIds(), to.getIds());
            }
        };

        config.withConfiguration().listener(adapter);
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
        return context -> context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
    }

    public Action<PaymentState, PaymentEvent> preAuthAction(){
        return context -> {
            BigDecimal amount = context.getExtendedState().get("amount", BigDecimal.class);
            Long paymentId = context.getExtendedState().get("paymentId", Long.class);
            System.out.println("PreAuth was called!!!");

            if (amount.compareTo(BigDecimal.valueOf(50000L)) < 0) {
                System.out.println("Initiate payment approved for paymentId " + paymentId + " with amount " + amount);
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.CREATE_PAYMENT)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());

            } else {
                System.out.println("Declined! Payment with id " + paymentId + ", amount = " + amount + " exceeds the limit.");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.DECLINE_PAYMENT)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }

    public Action<PaymentState, PaymentEvent> processPaymentAction(){
        return context -> {
            BigDecimal amount = context.getExtendedState().get("amount", BigDecimal.class);
            Long paymentId = context.getExtendedState().get("paymentId", Long.class);
            System.out.println("Process payment action called for payment with id = " + paymentId);
            if(Account.accountBalance.compareTo(amount) < 0) {
                System.out.println("Subtracting the money from the account");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.SUBTRACT_MONEY)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            } else {
                System.out.println("Declined, the amount is bigger than the balance.");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.DECLINE_PAYMENT)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }
}
