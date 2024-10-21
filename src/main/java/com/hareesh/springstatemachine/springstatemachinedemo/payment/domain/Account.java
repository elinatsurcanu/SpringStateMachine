package com.hareesh.springstatemachine.springstatemachinedemo.payment.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    public static BigDecimal accountBalance = new BigDecimal("1000.00");

    public static BigDecimal limitPerPayment = new BigDecimal("500.00");
}
