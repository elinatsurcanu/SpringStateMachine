package com.hareesh.springstatemachine.springstatemachinedemo.payment.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.CustomerOrder;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.OrderEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.OrderState;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.repository.OrderRepository;
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
public class OrderStateChangeInterceptor extends StateMachineInterceptorAdapter<OrderState, OrderEvent> {

    static final Logger LOGGER = LoggerFactory.getLogger(OrderStateChangeInterceptor.class);

    private final OrderRepository orderRepository;

    @Override
    public void preStateChange(State<OrderState, OrderEvent> state, Message<OrderEvent> message,
                               Transition<OrderState, OrderEvent> transition, StateMachine<OrderState, OrderEvent> stateMachine) {

        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((Long) msg.getHeaders().getOrDefault(
                OrderServiceImpl.PAYMENT_ID_HEADER, -1L))).ifPresent(paymentId -> {
            CustomerOrder customerOrder = orderRepository.getOrderByOrderId(paymentId);
            if (customerOrder != null) {
                OrderState fromState = customerOrder.getState();
                OrderState toState = state.getId();

                if (fromState != toState) {
                    customerOrder.setState(toState);
                    orderRepository.save(customerOrder);
                    LOGGER.info("Payment {} state changed from {} to {}", paymentId, fromState, toState);
                }
            }
        });
    }

}
