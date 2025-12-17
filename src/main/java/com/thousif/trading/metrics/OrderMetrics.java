package com.thousif.trading.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter orderCreatedCounter;
    private final Counter orderExecutedCounter;
    private final Counter orderCancelledCounter;
    private final Timer orderExecutionTimer;

    public OrderMetrics(MeterRegistry registry) {
        this.orderCreatedCounter = Counter.builder("orders.created")
                .description("Total orders created")
                .tag("type", "all")
                .register(registry);

        this.orderExecutedCounter = Counter.builder("orders.executed")
                .description("Total orders executed")
                .tag("type", "all")
                .register(registry);

        this.orderCancelledCounter = Counter.builder("orders.cancelled")
                .description("Total orders cancelled")
                .tag("type", "all")
                .register(registry);

        this.orderExecutionTimer = Timer.builder("orders.execution.time")
                .description("Order execution time")
                .register(registry);
    }

    public void incrementOrderCreated() {
        orderCreatedCounter.increment();
    }

    public void incrementOrderExecuted() {
        orderExecutedCounter.increment();
    }

    public void incrementOrderCancelled() {
        orderCancelledCounter.increment();
    }

    public Timer.Sample startExecutionTimer() {
        return Timer.start();
    }

    public void recordExecutionTime(Timer.Sample sample) {
        sample.stop(orderExecutionTimer);
    }
}