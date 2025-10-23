package com.thousif.trading.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlphaVantageService {

    @Value("${trading.market-data.alpha-vantage.api-key}")
    private String apiKey;

    @Value("${trading.market-data.alpha-vantage.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, BigDecimal> getStockPrice(String symbol){
        try{
            String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", baseUrl, symbol, apiKey);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode quoteNode = rootNode.get("Global Quote");

            if(quoteNode != null){
                Map<String, BigDecimal> stockData = new HashMap<>();
                stockData.put("price", new BigDecimal(quoteNode.get("05. price").asText()));
                stockData.put("change", new BigDecimal(quoteNode.get("09. change").asText()));
                stockData.put("changePercent",
                        new BigDecimal(quoteNode.get("10. change percent").asText().replace("%","")));
                log.info(stockData.toString());
                return stockData;
            }

        } catch (Exception e) {
            log.error("Error fetching stock data for symbol: {}", symbol, e);
        }
        return null;
    }

    public Map<String, Object> getHistoricalData(String symbol) {
        try {
            String url = String.format("%s/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s", baseUrl, symbol, apiKey);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");

            if(timeSeriesNode != null){
                Map<String, Object> historicalData = new HashMap<>();
                historicalData.put("symbol", symbol);
                historicalData.put("timeSeries", timeSeriesNode);
                return historicalData;
            }
        }
        catch (Exception e){
            log.error("Error fetching historical data for symbol: {}", symbol, e);
        }
        return null;
    }




}
