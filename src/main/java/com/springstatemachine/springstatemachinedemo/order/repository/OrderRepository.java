package com.springstatemachine.springstatemachinedemo.order.repository;

import com.springstatemachine.springstatemachinedemo.order.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    CustomerOrder getOrderByOrderId(Long orderId);

    List<CustomerOrder> findAll();

}
