package com.thousif.trading.enums;

public enum OrderStatus {
    PENDING,     // Order placed but not yet processed
    OPEN,        // Order is active in the market
    PARTIAL,     // Partially executed
    COMPLETE,    // Fully executed
    CANCELLED,   // Cancelled by user
    REJECTED     // Rejected due to validation/risk checks
}
