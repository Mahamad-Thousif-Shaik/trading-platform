package com.thousif.trading.repository;

import com.thousif.trading.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findByExchange(String exchange);

    List<Stock> findBySector(String sector);

    @Query("SELECT s FROM Stock s WHERE s.isActive = true AND " +
            "(LOWER(s.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.companyName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Stock> searchStocks(@Param("query") String query);

    List<Stock> findTop20ByOrderByVolumeDesc();

    List<Stock> findByIsActiveTrue();


}
