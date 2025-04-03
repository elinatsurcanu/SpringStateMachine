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

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final OrderRepository repository;

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    private final OrderStateChangeInterceptor orderStateChangeInterceptor;

    @Override
    public CustomerOrder getOrderById(Long paymentId) {
        return repository.getOrderByOrderId(paymentId);
    }

    @Override
    public CustomerOrder createNewOrder(CustomerOrder order) throws InsufficientFundsException {
       // customerOrder.setTotalCost(customerOrder.getTotalCost());
        order.setState(OrderState.INITIAL);

        BigDecimal totalCost = calculateTotalCost(order);
        order.setTotalCost(totalCost);

        CustomerOrder createdCustomerOrder = repository.save(order);

     //   StateMachine<OrderState, OrderEvent> sm = build(customerOrder.getId());
      //  preAuth(createdCustomerOrder.getId(), amount, sm);

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
    public void preAuth(Long paymentId, BigDecimal amount, StateMachine<OrderState, OrderEvent> sm) throws InsufficientFundsException {
        if (amount.compareTo(Account.limitPerPayment) <= 0) {
            sendEvent(paymentId, sm, OrderEvent.CREATE_ORDER);
        } else {
            sendEvent(paymentId, sm, OrderEvent.CANCEL_ORDER);
            LOGGER.error("Payment with id {} has been declined. The amount {} is bigger than {}", paymentId, amount, Account.limitPerPayment);
            throw new InsufficientFundsException(format("The amount is bigger than %s, payment is declined.", Account.limitPerPayment));
        }
    }


    private void sendEvent(Long paymentId, StateMachine<OrderState, OrderEvent> sm, OrderEvent event){
        Message<OrderEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);
    }

    private void initializeExtendedState(StateMachine<OrderState, OrderEvent> sm, BigDecimal amount, Long paymentId) {
        sm.getExtendedState().getVariables().put("amount", amount);
        sm.getExtendedState().getVariables().put("paymentId", paymentId);
    }

    private StateMachine<OrderState, OrderEvent> build(Long paymentId) {
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();

        CustomerOrder customerOrder = repository.getOne(paymentId);

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(orderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(customerOrder.getState(), null, null, null));
                });

        initializeExtendedState(stateMachine, customerOrder.getTotalCost(), paymentId);
        stateMachine.start();

        return stateMachine;
    }

    private BigDecimal calculateTotalCost(CustomerOrder order) {
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal count;
        BigDecimal price;

        for(int i = 0; i < order.getProducts().size(); i++) {
            count = order.getProducts().get(i).getCount();
            price = order.getProducts().get(i).getProduct().getProductPrice();
            totalCost = totalCost.add(count.multiply(price));
        }

        return totalCost;

    }
}
