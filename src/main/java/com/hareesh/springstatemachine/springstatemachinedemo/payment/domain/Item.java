package com.hareesh.springstatemachine.springstatemachinedemo.payment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Item {

    @JsonProperty("count")
    private int count;

    private Product product;
}
