package com.thousif.trading.controller;

import com.thousif.trading.entity.Portfolio;
import com.thousif.trading.service.trading.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<Portfolio>> getUserPortfolio(Authentication authentication) {
        List<Portfolio> portfolio = portfolioService.getUserPortfolio(authentication.getName());
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPortfolioSummary(Authentication authentication) {
        Map<String, Object> summary = portfolioService.getPortfolioSummary(authentication.getName());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getStockWisePerformance(Authentication authentication) {
        Map<String, Object> performance = portfolioService.getStockWisePerformance(authentication.getName());
        return ResponseEntity.ok(performance);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshPortfolioValues(Authentication authentication) {
        portfolioService.refreshPortfolioValues(authentication.getName());
        return ResponseEntity.ok("Portfolio values refreshed successfully");
    }

}
