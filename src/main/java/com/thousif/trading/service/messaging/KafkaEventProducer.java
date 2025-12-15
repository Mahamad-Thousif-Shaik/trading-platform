package com.thousif.trading.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    //private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String ORDER_TOPIC = "events.orders";
    private static final String NOTIFICATION_TOPIC = "events.notifications";

    public void publishOrderEvent(String key, Object payload) {
        send(ORDER_TOPIC, key, payload);
    }

    public void publishNotificationEvent(String userId, String key, Object payload) {
        send(NOTIFICATION_TOPIC, key, payload);
    }

    private void send(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            //kafkaTemplate.send(topic, key, json);
            log.debug("Published event to {} key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish event to {} key={}", topic, key, e);
        }
    }

}
