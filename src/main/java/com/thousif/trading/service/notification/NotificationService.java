package com.thousif.trading.service.notification;

import com.thousif.trading.entity.Order;
import com.thousif.trading.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;

    public void sendWelcomeNotifications(User user) {
        // Email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        // SMS
        if (user.getPhoneNumber() != null) {
            String message = String.format(
                    "Welcome to Trading Platform, %s! Your account is ready with ₹100,000 demo balance.",
                    user.getFirstName()
            );
            smsService.sendSms(user.getPhoneNumber(), message);
        }
    }

    public void sendOrderPlacedNotifications(Order order) {
        User user = order.getUser();

        // Email
        emailService.sendOrderConfirmationEmail(
                user.getEmail(),
                user.getUsername(),
                order.getOrderId(),
                order.getStock().getSymbol(),
                order.getTransactionType().toString(),
                order.getQuantity(),
                order.getPrice() != null ? order.getPrice().toString() : "Market Price"
        );

        // SMS
        if (user.getPhoneNumber() != null) {
            smsService.sendOrderNotification(
                    user.getPhoneNumber(),
                    order.getOrderId(),
                    order.getStock().getSymbol(),
                    order.getTransactionType().toString()
            );
        }
    }

    public void sendOrderExecutedNotifications(Order order, BigDecimal executedPrice) {
        User user = order.getUser();

        // Email
        emailService.sendOrderExecutionEmail(
                user.getEmail(),
                user.getUsername(),
                order.getOrderId(),
                order.getStock().getSymbol(),
                executedPrice.toString()
        );

        // SMS
        if (user.getPhoneNumber() != null) {
            smsService.sendOrderExecutionNotification(
                    user.getPhoneNumber(),
                    order.getOrderId(),
                    order.getStock().getSymbol(),
                    executedPrice.toString()
            );
        }
    }

    public void sendOrderCancelledNotifications(Order order) {
        User user = order.getUser();

        String subject = "Order Cancelled - " + order.getOrderId();
        String emailText = String.format(
                "Hello %s,\n\n" +
                        "Your order has been cancelled.\n\n" +
                        "Order ID: %s\n" +
                        "Stock: %s\n" +
                        "Quantity: %d\n\n" +
                        "Blocked margin has been released.\n\n" +
                        "Best regards,\n" +
                        "Trading Platform Team",
                user.getUsername(),
                order.getOrderId(),
                order.getStock().getSymbol(),
                order.getQuantity()
        );

        emailService.sendSimpleEmail(user.getEmail(), subject, emailText);

        if (user.getPhoneNumber() != null) {
            String smsText = String.format(
                    "Trading Platform: Order %s cancelled. Margin released.",
                    order.getOrderId()
            );
            smsService.sendSms(user.getPhoneNumber(), smsText);
        }
    }

}
