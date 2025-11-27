package com.thousif.trading.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${trading.notifications.sms.provider}")
    private String smsProvider;

    @Value("${trading.notifications.sms.from:TradingApp}")
    private String fromNumber;

    @Async
    public void sendSms(String toNumber, String message) {
        // For development: Log-based SMS (no external service needed)
        if ("log".equals(smsProvider)) {
            log.info("========== SMS NOTIFICATION ==========");
            log.info("To: {}", toNumber);
            log.info("From: {}", fromNumber);
            log.info("Message: {}", message);
            log.info("======================================");
            return;
        }

        // For production: Add actual SMS provider integration
        // Examples: Twilio, AWS SNS, Jasmin SMS Gateway, etc.
        try {
            sendViaSmsProvider(toNumber, message);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", toNumber, e);
        }
    }

    private void sendViaSmsProvider(String toNumber, String message) {
        // for production
        // example for Twilio:
        // twilioClient.messages.create(toNumber, fromNumber, message);

        log.warn("SMS provider not configured. Message logged only.");
    }

    public void sendOrderNotification(String phoneNumber, String orderId,
                                      String symbol, String transactionType) {
        String message = String.format(
                "Trading Platform: Your %s order for %s (ID: %s) has been placed successfully.",
                transactionType, symbol, orderId
        );
        sendSms(phoneNumber, message);
    }

    public void sendOrderExecutionNotification(String phoneNumber, String orderId,
                                               String symbol, String price) {
        String message = String.format(
                "Trading Platform: Order %s executed! %s at ₹%s. Check your portfolio.",
                orderId, symbol, price
        );
        sendSms(phoneNumber, message);
    }

    public void sendVerificationCode(String phoneNumber, String code) {
        String message = String.format(
                "Trading Platform: Your verification code is %s. Valid for 10 minutes.",
                code
        );
        sendSms(phoneNumber, message);
    }

}
