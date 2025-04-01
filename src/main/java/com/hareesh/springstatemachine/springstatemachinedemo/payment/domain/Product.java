package com.hareesh.springstatemachine.springstatemachinedemo.payment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {

    @JsonProperty("name")
    String productName;

    @JsonProperty("price")
    BigDecimal productPrice;
}
