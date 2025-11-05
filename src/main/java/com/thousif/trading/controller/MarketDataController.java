package com.thousif.trading.controller;

import com.thousif.trading.service.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    @MessageMapping("/subscribe")
    @SendTo("/topic/market-data")
    public String subscribeToMarketData(){
        return "Subscribed to market data updates";
    }

    @MessageMapping("/subscribe/{symbol}")
    @SendTo("/topic/stock/{symbol}")
    public Map<String, Object> subscribeToStock(@DestinationVariable String symbol){
        return marketDataService.getLatestMarketData(symbol);
    }

    @GetMapping("/api/market-data/{symbol}")
    @ResponseBody
    public Map<String, Object> getLatestMarketData(@PathVariable String symbol){
        return marketDataService.getLatestMarketData(symbol.toUpperCase());
    }

}
