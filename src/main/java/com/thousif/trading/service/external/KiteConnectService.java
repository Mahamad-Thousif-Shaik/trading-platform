package com.thousif.trading.service.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiteConnectService {

    @Value("${trading.kite.api-key}")
    private String apiKey;
    @Value("${trading.kite.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getInstruments(String symbol){
        try{
            String url = baseUrl + "/instruments";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", "token " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
        }
        catch (Exception e){
            log.error("Error fetching instruments from Kite Connect", e);
            return null;
        }
    }

    public Map<String, Object> getQuote(String symbol){
        try{
            String url = String.format("%s/quote?i=NSE:%s", baseUrl, symbol);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Kite-Version", "3");
            headers.set("Authorization", "token " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
        }
        catch (Exception e){
            log.error("Error fetching quote for symbol: {}", symbol, e);
            return null;
        }
    }

}
