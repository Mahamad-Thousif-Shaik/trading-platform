package com.thousif.trading.service.market;

import com.thousif.trading.entity.Stock;
import com.thousif.trading.service.cache.CacheService;
import com.thousif.trading.service.trading.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Slf4j
public class MarketDataService {

    private final StockService stockService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CacheService cacheService;
    private final Random random = new Random();

    @Scheduled(fixedRate = 5000)
    public void broadcastMarketData(){

        List<Stock> popularStocks = stockService.getPopularStocks();

        for(Stock stock : popularStocks.subList(0, Math.min(10, popularStocks.size()))){
            try{
                Map<String, Object> priceUpdate = generatePriceUpdate(stock);

                //cache update

                String cacheKey = "market-data:" + stock.getSymbol();
                cacheService.cacheObject(cacheKey, priceUpdate, Duration.ofMinutes(1));

                //Broadcast to WebSocket subscribers
                messagingTemplate.convertAndSend("/topic/market-data", priceUpdate);
                messagingTemplate.convertAndSend("/topic/stock/" + stock.getSymbol(), priceUpdate);

                log.debug("Broadcasted market data for: {}", stock.getSymbol());
            } catch (Exception e) {
                log.error("Error broadcasting market data for stock: {}", stock.getSymbol(), e);
            }
        }

    }

    private Map<String, Object> generatePriceUpdate(Stock stock){
        BigDecimal currentPrice = stock.getCurrentPrice();
        if(currentPrice == null){
            currentPrice = new BigDecimal("100.00");//Default price for demo
        }

        //Simulate realistic price changes (+-2%)
        double changePercent = (random.nextDouble() - 0.5) * 0.04; //-2% t0 +2%
        BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));
        BigDecimal newPrice = currentPrice.add(change).setScale(2, RoundingMode.HALF_UP);

        // Ensure price doesn't go negative
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            newPrice = currentPrice.multiply(new BigDecimal("0.99"));
        }

        return Map.of(
            "symbol", stock.getSymbol(),
                "companyName", stock.getCompanyName(),
                "price", newPrice,
                "previousPrice", currentPrice,
                "change", change.setScale(2, RoundingMode.HALF_UP),
                "changePercent", BigDecimal.valueOf(changePercent * 100).setScale(2, RoundingMode.HALF_UP),
                "volume", stock.getVolume() != null ? stock.getVolume() : 0L,
                "timestamp", LocalDateTime.now(),
                "exchange", stock.getExchange()
        );
    }

    @Scheduled(fixedRate = 60000)
    public void updateStockPrices(){
        List<Stock> popularStocks = stockService.getPopularStocks();

        for(Stock stock: popularStocks.subList(0, Math.min(5, popularStocks.size()))){
            try{
                stockService.updateStockPrice(stock.getSymbol());
            }
            catch (Exception e){
                log.error("Error updating stock price for: {}", stock.getSymbol(), e);
            }
        }
    }

    public Map<String, Object> getLatestMarketData(String symbol){
        String cacheKey = "market-data:" + symbol;
        Object cacheData = cacheService.getCacheObject(cacheKey);
        if(cacheData instanceof Map){
            return (Map<String, Object>) cacheData;
        }

        Stock stock = stockService.getStockBySymbol(symbol);
        return generatePriceUpdate(stock);
    }

}
