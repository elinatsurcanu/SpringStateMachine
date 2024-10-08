package com.hareesh.springstatemachine.springstatemachinedemo.payment.exception;

public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(String msg) {
        super(msg);
    }

}
