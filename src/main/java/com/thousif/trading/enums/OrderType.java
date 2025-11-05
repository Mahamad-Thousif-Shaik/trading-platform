package com.thousif.trading.enums;

public enum OrderType {
    MARKET,    // Execute at current market price
    LIMIT,     // Execute at specified price or better
    SL,        // Stop Loss order
    SL_M       // Stop Loss Market order
}
