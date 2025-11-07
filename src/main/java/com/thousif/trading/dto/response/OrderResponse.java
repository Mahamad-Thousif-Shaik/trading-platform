package com.thousif.trading.dto.response;

import com.thousif.trading.enums.OrderStatus;
import com.thousif.trading.enums.OrderType;
import com.thousif.trading.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderId;
    private String kiteOrderId;
    private String stockSymbol;
    private String stockName;
    private TransactionType transactionType;
    private OrderType orderType;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal executedPrice;
    private Integer executedQuantity;
    private OrderStatus status;
    private String validity;
    private Integer disclosedQuantity;
    private BigDecimal triggerPrice;
    private String notes;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime executedAt;
    private String message;

    // Calculated fields
    private BigDecimal totalValue;
    private BigDecimal executedValue;
    private Integer remainingQuantity;

}
