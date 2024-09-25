package com.hareesh.springstatemachine.springstatemachinedemo.repository;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment getPaymentById(Long paymentId);

    List<Payment> findAll();

}
