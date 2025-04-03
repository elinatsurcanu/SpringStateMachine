package com.hareesh.springstatemachine.springstatemachinedemo.payment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Product implements Serializable {

    @JsonProperty("name")
    String productName;

    @JsonProperty("price")
    BigDecimal productPrice;
}
