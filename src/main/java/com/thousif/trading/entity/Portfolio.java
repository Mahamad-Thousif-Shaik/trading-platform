package com.thousif.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "average_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(name = "invested_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal investedAmount = BigDecimal.ZERO;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(name = "unrealized_pnl", precision = 15, scale = 2)
    private BigDecimal unrealizedPnl = BigDecimal.ZERO;

    @Column(name = "realized_pnl", precision = 15, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
