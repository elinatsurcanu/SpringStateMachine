package com.hareesh.springstatemachine.springstatemachinedemo.payment.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    final static Logger LOGGER = LoggerFactory.getLogger(PaymentStateChangeInterceptor.class);

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine) {

        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((Long) msg.getHeaders().getOrDefault(
                PaymentServiceImpl.PAYMENT_ID_HEADER, -1L))).ifPresent(paymentId -> {
            LOGGER.info("Interceptor at work before the transition from state {} to {}",
                    transition.getSource() != null ? transition.getSource().getId() : "Unknown",
                    state.getId());
            Payment payment = paymentRepository.getOne(paymentId);
            payment.setState(state.getId());
            paymentRepository.save(payment);
        });
    }

}
