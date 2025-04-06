package com.springstatemachine.springstatemachinedemo.order.exception;

public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(String msg) {
        super(msg);
    }

}
