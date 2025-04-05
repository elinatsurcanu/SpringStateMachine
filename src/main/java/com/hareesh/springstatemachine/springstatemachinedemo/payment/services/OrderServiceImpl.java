package com.hareesh.springstatemachine.springstatemachinedemo.payment.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.*;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.PaymentException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    public static final String ORDER_ID_HEADER = "order_id";

    private final OrderRepository repository;

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    private final OrderStateChangeInterceptor orderStateChangeInterceptor;

    @Override
    public CustomerOrder getOrderById(Long paymentId) {
        return repository.getOrderByOrderId(paymentId);
    }

    @Override
    public CustomerOrder createNewOrder(CustomerOrder order) throws InsufficientFundsException {
        order.setState(OrderState.INITIAL);

        BigDecimal totalCost = calculateTotalCost(order);
        order.setTotalCost(totalCost);

        CustomerOrder createdCustomerOrder = repository.save(order);

        StateMachine<OrderState, OrderEvent> sm = build(createdCustomerOrder.getOrderId());
        preAuth(createdCustomerOrder, sm);

        return createdCustomerOrder;
    }

    @Override
    public CustomerOrder processOrder(Long paymentId) throws InsufficientFundsException, PaymentException {
        CustomerOrder customerOrder = getOrderById(paymentId);

        if (customerOrder == null) {
            LOGGER.error("Payment with id {} not found", paymentId);
            throw new PaymentException("Payment with id " + paymentId + " not found");
        } else if (OrderState.PROCESSED.equals(customerOrder.getState()) ||
                OrderState.CANCELLED.equals(customerOrder.getState())) {
            LOGGER.error("Payment with id {} already processed", paymentId);
            throw new PaymentException("Payment with id " + paymentId + " already processed");
        }

        StateMachine<OrderState, OrderEvent> sm = build(paymentId);
        if (customerOrder.getTotalCost().compareTo(Account.accountBalance) <= 0) {
            Account.accountBalance = Account.accountBalance.subtract(customerOrder.getTotalCost());
            sendEvent(paymentId, sm, OrderEvent.PROCESS_ORDER);
        } else {
            sendEvent(paymentId, sm, OrderEvent.CANCEL_ORDER);
            LOGGER.error("Payment with id {} has been declined. Not enough money", paymentId);
            throw new InsufficientFundsException("Not enough balance");
        }
        return customerOrder;
    }

    @Override
    public List<CustomerOrder> getAllOrders() {
        return repository.findAll();
    }

    @Override
    public void preAuth(CustomerOrder customerOrder, StateMachine<OrderState, OrderEvent> sm) throws InsufficientFundsException {
        if (customerOrder.getTotalCost().compareTo(Account.limitPerPayment) <= 0) {
            sendEvent(customerOrder.getOrderId(), sm, OrderEvent.CREATE_ORDER);
        } else {
            sendEvent(customerOrder.getOrderId(), sm, OrderEvent.CANCEL_ORDER);
            LOGGER.error("Order with id {} has been declined. The amount {} is bigger than {}", customerOrder.getOrderId(), customerOrder.getTotalCost(), Account.limitPerPayment);
            throw new InsufficientFundsException(format("The total cost is bigger than %s, the order is cancelled.", Account.limitPerPayment));
        }
    }


    private void sendEvent(Long orderId, StateMachine<OrderState, OrderEvent> sm, OrderEvent event){
        Message<OrderEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(ORDER_ID_HEADER, orderId)
                .build();

        sm.sendEvent(msg);
    }

    private void initializeExtendedState(StateMachine<OrderState, OrderEvent> sm, CustomerOrder order) {
        sm.getExtendedState().getVariables().put("orderId", order.getOrderId());
        sm.getExtendedState().getVariables().put("products", order.getProducts());
        sm.getExtendedState().getVariables().put("totalCost", order.getTotalCost());
    }

    private StateMachine<OrderState, OrderEvent> build(Long orderId) {
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();

        CustomerOrder customerOrder = repository.getOne(orderId);

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(orderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(customerOrder.getState(), null, null, null));
                });

        initializeExtendedState(stateMachine, customerOrder);
        stateMachine.start();

        return stateMachine;
    }

    private BigDecimal calculateTotalCost(CustomerOrder order) {
        return order.getProducts().stream()
                .map(item -> item.getCount().multiply(item.getProduct().getProductPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
