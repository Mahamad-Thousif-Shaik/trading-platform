package com.thousif.trading.service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private static final String ORDER_TOPIC = "events.orders";
    private static final String NOTIFICATION_TOPIC = "events.notifications";

    //@KafkaListener(topics = ORDER_TOPIC, groupId = "trading-platform")
    public void consumeOrderEvent(String message) {
        log.info("Received order event: {}", message);
        // Process order event
        // Could trigger analytics, logging, external integrations, etc.
    }

    //@KafkaListener(topics = NOTIFICATION_TOPIC, groupId = "trading-platform")
    public void consumeNotificationEvent(String message) {
        log.info("Received notification event: {}", message);
        // Process notification event
        // Could send to external notification service, store in DB, etc.
    }
}
