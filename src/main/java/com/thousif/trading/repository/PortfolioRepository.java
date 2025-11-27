package com.thousif.trading.repository;

import com.thousif.trading.entity.Portfolio;
import com.thousif.trading.entity.Stock;
import com.thousif.trading.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserAndStock(User user, Stock stock);

    List<Portfolio> findByUserAndQuantityGreaterThan(User user, Integer quantity);

    List<Portfolio> findByUser(User user);

    @Query("SELECT SUM(p.currentValue) FROM Portfolio p WHERE p.user = :user AND p.quantity > 0")
    BigDecimal getTotalPortfolioValue(@Param("user") User user);

    @Query("SELECT SUM(p.investedAmount) FROM Portfolio p WHERE p.user = :user AND p.quantity > 0")
    BigDecimal getTotalInvestedAmount(@Param("user") User user);

    @Query("SELECT SUM(p.unrealizedPnl) FROM Portfolio p WHERE p.user = :user AND p.quantity > 0")
    BigDecimal getTotalUnrealizedPnl(@Param("user") User user);

    @Query("SELECT SUM(p.investedAmount) FROM Portfolio p WHERE p.user = :user AND p.quantity > 0")
    BigDecimal getTotalRealizedPnl(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.user = :user AND p.quantity > 0")
    long countActiveHoldings(@Param("user") User user);
}
