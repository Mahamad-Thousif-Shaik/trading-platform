package com.thousif.trading.service.trading;

import com.thousif.trading.entity.Portfolio;
import com.thousif.trading.entity.Stock;
import com.thousif.trading.entity.User;
import com.thousif.trading.repository.PortfolioRepository;
import com.thousif.trading.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserService userService;

    @Transactional
    public void updatePortfolioOnExecution(User user, Stock stock, Integer quantity,
                                           BigDecimal executedPrice, String transactionType){

        log.info("Updating portfolio for user: {}, stock: {}, quantity: {}, price: {}, type: {}",
                user.getUsername(), stock.getSymbol(), quantity, executedPrice, transactionType);

        Portfolio portfolio = portfolioRepository.findByUserAndStock(user, stock)
                .orElse(Portfolio.builder()
                        .user(user)
                        .stock(stock)
                        .quantity(0)
                        .averagePrice(BigDecimal.ZERO)
                        .investedAmount(BigDecimal.ZERO)
                        .realizedPnl(BigDecimal.ZERO)
                        .build());

        if ("BUY".equals(transactionType)) {
            updatePortfolioForBuy(portfolio, quantity, executedPrice);
        } else if ("SELL".equals(transactionType)) {
            updatePortfolioForSell(portfolio, quantity, executedPrice);
        }

        calculateCurrentValues(portfolio);
        portfolioRepository.save(portfolio);

        log.info("Portfolio updated successfully for user: {}, stock: {}",
                user.getUsername(), stock.getSymbol());
    }

    private void updatePortfolioForBuy(Portfolio portfolio, Integer quantity, BigDecimal price) {
        BigDecimal tradeValue = price.multiply(new BigDecimal(quantity));
        BigDecimal newInvestedAmount = portfolio.getInvestedAmount().add(tradeValue);
        Integer newQuantity = portfolio.getQuantity() + quantity;
        BigDecimal newAveragePrice = newQuantity > 0
                ? newInvestedAmount.divide(new BigDecimal(newQuantity), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        portfolio.setQuantity(newQuantity);
        portfolio.setAveragePrice(newAveragePrice);
        portfolio.setInvestedAmount(newInvestedAmount);

        log.debug("Buy execution - Quantity: {}, Average Price: {}, Invested: {}",
                newQuantity, newAveragePrice, newInvestedAmount);
    }

    private void updatePortfolioForSell(Portfolio portfolio, Integer quantity, BigDecimal price) {
        if (portfolio.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stocks to sell");
        }

        Integer newQuantity = portfolio.getQuantity() - quantity;
        BigDecimal saleValue = price.multiply(new BigDecimal(quantity));
        BigDecimal soldCostBasis = portfolio.getAveragePrice().multiply(new BigDecimal(quantity));

        // Calculate realized P&L
        BigDecimal realizedPnl = saleValue.subtract(soldCostBasis);
        portfolio.setRealizedPnl(portfolio.getRealizedPnl().add(realizedPnl));

        // Update quantity and invested amount
        portfolio.setQuantity(newQuantity);
        if (newQuantity > 0) {
            BigDecimal remainingInvestedAmount = portfolio.getAveragePrice()
                    .multiply(new BigDecimal(newQuantity));
            portfolio.setInvestedAmount(remainingInvestedAmount);
        } else {
            portfolio.setInvestedAmount(BigDecimal.ZERO);
            portfolio.setAveragePrice(BigDecimal.ZERO);
        }

        log.debug("Sell execution - Quantity: {}, Realized P&L: {}, Remaining Invested: {}",
                newQuantity, realizedPnl, portfolio.getInvestedAmount());
    }

    private void calculateCurrentValues(Portfolio portfolio) {
        if (portfolio.getQuantity() > 0) {
            BigDecimal currentPrice = portfolio.getStock().getCurrentPrice();
            if (currentPrice != null) {
                BigDecimal currentValue = currentPrice.multiply(new BigDecimal(portfolio.getQuantity()));
                BigDecimal unrealizedPnl = currentValue.subtract(portfolio.getInvestedAmount());

                portfolio.setCurrentValue(currentValue);
                portfolio.setUnrealizedPnl(unrealizedPnl);
            }
        } else {
            portfolio.setCurrentValue(BigDecimal.ZERO);
            portfolio.setUnrealizedPnl(BigDecimal.ZERO);
        }
    }

    public List<Portfolio> getUserPortfolio(String username){
        User user = userService.findByUsername(username);
        List<Portfolio> holdings = portfolioRepository.findByUserAndQuantityGreaterThan(user, 0);

        // Update current values with latest prices
        holdings.forEach(this::calculateCurrentValues);

        return holdings;
    }

    public Map<String, Object> getPortfolioSummary(String username) {
        User user = userService.findByUsername(username);

        BigDecimal totalValue = portfolioRepository.getTotalPortfolioValue(user);
        BigDecimal totalInvested = portfolioRepository.getTotalInvestedAmount(user);
        BigDecimal totalUnrealizedPnl = portfolioRepository.getTotalUnrealizedPnl(user);
        BigDecimal totalRealizedPnl = portfolioRepository.getTotalRealizedPnl(user);
        long activeHoldings = portfolioRepository.countActiveHoldings(user);

        // Calculate total P&L and percentage returns
        BigDecimal totalPnl = (totalUnrealizedPnl != null ? totalUnrealizedPnl : BigDecimal.ZERO)
                .add(totalRealizedPnl != null ? totalRealizedPnl : BigDecimal.ZERO);

        BigDecimal totalReturnPercent = BigDecimal.ZERO;
        if (totalInvested != null && totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnPercent = totalPnl.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalValue", totalValue != null ? totalValue : BigDecimal.ZERO);
        summary.put("totalInvested", totalInvested != null ? totalInvested : BigDecimal.ZERO);
        summary.put("totalUnrealizedPnl", totalUnrealizedPnl != null ? totalUnrealizedPnl : BigDecimal.ZERO);
        summary.put("totalRealizedPnl", totalRealizedPnl != null ? totalRealizedPnl : BigDecimal.ZERO);
        summary.put("totalPnl", totalPnl);
        summary.put("totalReturnPercent", totalReturnPercent);
        summary.put("activeHoldings", activeHoldings);
        summary.put("availableBalance", user.getAvailableBalance());
        summary.put("usedMargin", user.getUsedMargin());
        summary.put("freeMargin", user.getFreeMargin());

        return summary;
    }

    public Integer getAvailableQuantity(User user, Stock stock) {
        return portfolioRepository.findByUserAndStock(user, stock)
                .map(Portfolio::getQuantity)
                .orElse(0);
    }

    public Map<String, Object> getStockWisePerformance(String username) {
        User user = userService.findByUsername(username);
        List<Portfolio> portfolio = portfolioRepository.findByUser(user);

        Map<String, Object> performance = new HashMap<>();

        for (Portfolio holding : portfolio) {
            if (holding.getQuantity() > 0 || holding.getRealizedPnl().compareTo(BigDecimal.ZERO) != 0) {
                Map<String, Object> stockData = new HashMap<>();
                stockData.put("symbol", holding.getStock().getSymbol());
                stockData.put("companyName", holding.getStock().getCompanyName());
                stockData.put("quantity", holding.getQuantity());
                stockData.put("averagePrice", holding.getAveragePrice());
                stockData.put("currentPrice", holding.getStock().getCurrentPrice());
                stockData.put("investedAmount", holding.getInvestedAmount());
                stockData.put("currentValue", holding.getCurrentValue());
                stockData.put("unrealizedPnl", holding.getUnrealizedPnl());
                stockData.put("realizedPnl", holding.getRealizedPnl());

                // Calculate percentage return
                BigDecimal returnPercent = getReturnPercent(holding);
                stockData.put("returnPercent", returnPercent);

                performance.put(holding.getStock().getSymbol(), stockData);
            }
        }

        return performance;
    }

    private BigDecimal getReturnPercent(Portfolio holding) {
        BigDecimal totalPnl = holding.getUnrealizedPnl().add(holding.getRealizedPnl());
        BigDecimal totalInvested = holding.getInvestedAmount().add(
                holding.getRealizedPnl().compareTo(BigDecimal.ZERO) > 0
                        ? holding.getRealizedPnl() : BigDecimal.ZERO
        );

        BigDecimal returnPercent = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            returnPercent = totalPnl.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        return returnPercent;
    }

    @Transactional
    public void refreshPortfolioValues(String username) {
        User user = userService.findByUsername(username);
        List<Portfolio> portfolio = portfolioRepository.findByUser(user);

        for (Portfolio holding : portfolio) {
            calculateCurrentValues(holding);
        }

        portfolioRepository.saveAll(portfolio);
        log.info("Refreshed portfolio values for user: {}", username);
    }

}
