package com.thousif.trading.actuator;

import com.thousif.trading.service.trading.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    private final StockService stockService;

    @Override
    public Health health() {
        try {
            long activeStocks = stockService.getActiveStocks().size();
            log.debug(activeStocks+"");
            if (activeStocks > 0) {
                log.debug("health check hit");
                return Health.up()
                        .withDetail("activeStocks", activeStocks)
                        .withDetail("message", "Stock service operational")
                        .build();
            } else {
                log.debug("health check hit - else block");
                return Health.down()
                        .withDetail("activeStocks", 0)
                        .withDetail("message", "No active stocks available")
                        .build();
            }
        } catch (Exception e) {
            log.debug("health check hit - exception");
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
