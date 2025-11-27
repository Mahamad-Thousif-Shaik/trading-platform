package com.thousif.trading.service.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("$trading.notifications.email.from")
    private String fromEmail;

    @Async
    public void sendSimpleEmail(String to, String subject, String text){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
        }
    }

    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Trading Platform!";
        String text = String.format(
                "Hello %s,\n\n" +
                        "Welcome to our Trading Platform!\n\n" +
                        "Your account has been successfully created.\n" +
                        "Demo Balance: ₹100,000\n\n" +
                        "You can now start trading stocks.\n\n" +
                        "Best regards,\n" +
                        "Trading Platform Team",
                username
        );

        sendSimpleEmail(to, subject, text);
    }

    public void sendOrderConfirmationEmail(String to, String username, String orderId,
                                           String symbol, String transactionType,
                                           int quantity, String price) {
        String subject = "Order Confirmation - " + orderId;
        String text = String.format(
                "Hello %s,\n\n" +
                        "Your order has been placed successfully!\n\n" +
                        "Order ID: %s\n" +
                        "Stock: %s\n" +
                        "Type: %s\n" +
                        "Quantity: %d\n" +
                        "Price: ₹%s\n\n" +
                        "You can view your order details in your account.\n\n" +
                        "Best regards,\n" +
                        "Trading Platform Team",
                username, orderId, symbol, transactionType, quantity, price
        );

        sendSimpleEmail(to, subject, text);
    }

    public void sendOrderExecutionEmail(String to, String username, String orderId,
                                        String symbol, String executedPrice) {
        String subject = "Order Executed - " + orderId;
        String text = String.format(
                "Hello %s,\n\n" +
                        "Your order has been executed!\n\n" +
                        "Order ID: %s\n" +
                        "Stock: %s\n" +
                        "Executed Price: ₹%s\n\n" +
                        "Check your portfolio for updated holdings.\n\n" +
                        "Best regards,\n" +
                        "Trading Platform Team",
                username, orderId, symbol, executedPrice
        );

        sendSimpleEmail(to, subject, text);
    }

}
