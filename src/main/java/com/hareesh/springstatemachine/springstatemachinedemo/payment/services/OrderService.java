package com.hareesh.springstatemachine.springstatemachinedemo.payment.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.CustomerOrder;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.OrderEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.OrderState;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.PaymentException;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

   void preAuth(Long paymentId, BigDecimal amount, StateMachine<OrderState, OrderEvent> sm) throws InsufficientFundsException;

    CustomerOrder getOrderById(Long paymentId);

    CustomerOrder createNewOrder(CustomerOrder customerOrder) throws InsufficientFundsException;

    CustomerOrder processOrder(Long paymentId) throws InsufficientFundsException, PaymentException;

    List<CustomerOrder> getAllOrders();

}
