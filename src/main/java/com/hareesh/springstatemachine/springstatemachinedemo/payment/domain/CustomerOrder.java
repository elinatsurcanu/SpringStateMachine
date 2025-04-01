package com.hareesh.springstatemachine.springstatemachinedemo.payment.domain;


import com.hareesh.springstatemachine.springstatemachinedemo.util.HashMapConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    @Convert(converter = HashMapConverter.class)
    private HashMap<Integer, Product> products;

    private BigDecimal totalCost;

}
