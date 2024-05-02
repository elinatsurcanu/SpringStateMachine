package com.hareesh.springstatemachine.springstatemachinedemo.repository;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment getPaymentById(Long paymentId);

    @Query(value = "select p from Payment p")
    List<Payment> getPayments();


}
