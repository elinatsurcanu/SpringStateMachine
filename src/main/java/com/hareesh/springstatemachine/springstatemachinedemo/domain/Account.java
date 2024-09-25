package com.hareesh.springstatemachine.springstatemachinedemo.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    public static BigDecimal accountBalance = new BigDecimal("100.00");
}
