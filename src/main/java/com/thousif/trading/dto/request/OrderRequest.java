package com.thousif.trading.dto.request;

import com.thousif.trading.enums.OrderType;
import com.thousif.trading.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Stock symbol is required")
    private String stockSymbol;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    private Integer quantity;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price; // Required for LIMIT orders

    @DecimalMin(value = "0.01", message = "Trigger price must be greater than 0")
    private BigDecimal triggerPrice; // Required for SL orders

    @Pattern(regexp = "DAY|IOC|GTD", message = "Validity must be DAY, IOC, or GTD")
    private String validity;

    @Min(value = 0, message = "Disclosed quantity cannot be negative")
    private Integer disclosedQuantity;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

}
