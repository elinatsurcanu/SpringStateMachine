package com.springstatemachine.springstatemachinedemo.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.springstatemachine.springstatemachinedemo.order.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderValidator.class);

    private static final Map<String, BigDecimal> AVAILABLE_STOCK = new HashMap<>();

    static {
        AVAILABLE_STOCK.put("banana", BigDecimal.valueOf(10));
        AVAILABLE_STOCK.put("apple", BigDecimal.valueOf(20));
        AVAILABLE_STOCK.put("pear", BigDecimal.valueOf(15));
    }

    public static boolean isCountWithinTheLimit(List<Item> items) {
        boolean allInStock = true;

        for (Item item : items) {
            String name = item.getProduct().getProductName().toLowerCase();
            BigDecimal requested = item.getCount();
            BigDecimal available = AVAILABLE_STOCK.getOrDefault(name, BigDecimal.ZERO);
            if (requested.compareTo(available) > 0) {
                allInStock = false;
                LOGGER.warn("The requested quantity of the product {} exceeds available stock", name);
            }
        }

        return allInStock;
    }

}
