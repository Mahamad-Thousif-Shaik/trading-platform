package com.thousif.trading.exception;

public class TradingPlatformException extends RuntimeException{

    public TradingPlatformException(String message){
        super(message);
    }

    public TradingPlatformException(String message, Throwable cause){
        super(message, cause);
    }

}
