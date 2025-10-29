package com.thousif.trading.service.trading;

import com.thousif.trading.entity.Stock;
import com.thousif.trading.repository.StockRepository;
import com.thousif.trading.service.cache.CacheService;
import com.thousif.trading.service.external.AlphaVantageService;
import com.thousif.trading.service.external.KiteConnectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final AlphaVantageService alphaVantageService;
    private final KiteConnectService kiteConnectService;
    private final CacheService cacheService;

    @Cacheable(value = "stocks", key = "#symbol")
    public Stock getStockBySymbol(String symbol){

        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        log.debug("db hit");
        return stock;
    }

    public List<Stock> searchStocks(String query){
        return stockRepository.searchStocks(query);
    }

    public List<Stock> getActiveStocks(){
        return stockRepository.findByIsActiveTrue();
    }

    public List<Stock> getPopularStocks(){
        return stockRepository.findTop20ByOrderByVolumeDesc();
    }

    public Map<String, BigDecimal> getLivePrice(String symbol){
        return alphaVantageService.getStockPrice(symbol);
    }

    public Stock updateStockPrice(String symbol){
        Stock stock = getStockBySymbol(symbol);
        Map<String, BigDecimal> priceData = getLivePrice(symbol);

        if(priceData != null && priceData.containsKey("price")){
            stock.setCurrentPrice(priceData.get("price"));
            stock = stockRepository.save(stock);
            log.debug("Updated price for {}: {}",symbol, priceData.get("price"));
        }
        return stock;
    }

    public List<Stock> getStocksBySector(String sector){
        return stockRepository.findBySector(sector);
    }

    public List<Stock> getStockByExchange(String exchange){
        return stockRepository.findByExchange(exchange);
    }

}
