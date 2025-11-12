package com.thousif.trading.exception;

public class InsufficientFundsException extends TradingPlatformException{
    public InsufficientFundsException(String message) {
        super(message);
    }
}
