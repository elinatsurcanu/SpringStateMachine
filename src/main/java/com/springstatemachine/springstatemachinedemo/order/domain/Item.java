package com.springstatemachine.springstatemachinedemo.order.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Item {

    private BigDecimal count;

    private Product product;
}
