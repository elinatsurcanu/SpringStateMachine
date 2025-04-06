package com.springstatemachine.springstatemachinedemo.order.services;

import com.springstatemachine.springstatemachinedemo.order.domain.CustomerOrder;
import com.springstatemachine.springstatemachinedemo.order.domain.OrderEvent;
import com.springstatemachine.springstatemachinedemo.order.domain.OrderState;
import com.springstatemachine.springstatemachinedemo.order.exception.InsufficientFundsException;
import com.springstatemachine.springstatemachinedemo.order.exception.OrderException;
import org.springframework.statemachine.StateMachine;

import java.util.List;

public interface OrderService {

   void preAuth(CustomerOrder customerOrder, StateMachine<OrderState, OrderEvent> sm) throws InsufficientFundsException;

    CustomerOrder getOrderById(Long orderId);

    CustomerOrder createNewOrder(CustomerOrder customerOrder) throws InsufficientFundsException;

    CustomerOrder processOrder(Long orderId) throws InsufficientFundsException, OrderException;

    List<CustomerOrder> getAllOrders();

}
