package com.thousif.trading.controller;

import com.thousif.trading.entity.Stock;
import com.thousif.trading.service.trading.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<Stock>> getAllActiveStocks(){
        List<Stock> stocks = stockService.getActiveStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Stock>> getPopularStocks(){
        List<Stock> stocks = stockService.getPopularStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> getStock(@PathVariable String symbol){
        Stock stock = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStock(@RequestParam String q){
        List<Stock> stocks = stockService.searchStocks(q);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<Stock>> getStocksBySector(@PathVariable String sector){
        List<Stock> stocks = stockService.getStocksBySector(sector);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/exchange/{exchange}")
    public ResponseEntity<List<Stock>> getStocksByExchange(@PathVariable String exchange){
        List<Stock> stocks = stockService.getStockByExchange(exchange.toUpperCase());
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{symbol}/price")
    public ResponseEntity<Map<String, BigDecimal>> getStockPrice(@PathVariable String symbol){
        Map<String, BigDecimal> priceDetails = stockService.getLivePrice(symbol);
        return ResponseEntity.ok(priceDetails);
    }

    @PostMapping("/{symbol}/refresh")
    public ResponseEntity<Stock> refreshStockPrice(@PathVariable String symbol){
        Stock stock = stockService.updateStockPrice(symbol);
        return ResponseEntity.ok(stock);
    }




}
