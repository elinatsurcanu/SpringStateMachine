package com.hareesh.springstatemachine.springstatemachinedemo.payment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Item {

    @JsonProperty("count")
    private BigDecimal count;

    private Product product;
}
