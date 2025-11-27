package com.thousif.trading.service.trading;

import com.thousif.trading.dto.request.OrderRequest;
import com.thousif.trading.dto.response.OrderResponse;
import com.thousif.trading.entity.Order;
import com.thousif.trading.entity.Stock;
import com.thousif.trading.entity.User;
import com.thousif.trading.enums.OrderStatus;
import com.thousif.trading.enums.OrderType;
import com.thousif.trading.enums.TransactionType;
import com.thousif.trading.exception.InsufficientFundsException;
import com.thousif.trading.exception.OrderValidationException;
import com.thousif.trading.exception.TradingPlatformException;
import com.thousif.trading.repository.OrderRepository;
import com.thousif.trading.service.auth.UserService;
import com.thousif.trading.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final StockService stockService;
    private final PortfolioService portfolioService;
    private final EmailService emailService;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String username){
        log.info("Placing order for user: {}, stock: {}, type: {}, quantity: {}",
                username, request.getStockSymbol(), request.getTransactionType(), request.getQuantity());

        User user = userService.findByUsername(username);
        Stock stock = stockService.getStockBySymbol(request.getStockSymbol());

        //validate order
        validateOrder(request, user, stock);

        //creating order
        Order order = Order.builder()
                .orderId(generateOrderId())
                .user(user)
                .stock(stock)
                .transactionType(request.getTransactionType())
                .orderType(request.getOrderType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .triggerPrice(request.getTriggerPrice())
                .validity(request.getValidity())
                .disclosedQuantity(request.getDisclosedQuantity())
                .notes(request.getNotes())
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);

        // Process order based on type
        processOrder(order);

        // Send order confirmation email
        emailService.sendOrderConfirmationEmail(
                user.getEmail(),
                user.getUsername(),
                order.getOrderId(),
                stock.getSymbol(),
                request.getTransactionType().toString(),
                request.getQuantity(),
                request.getPrice() != null ? request.getPrice().toString() : "Market Price"
        );

        log.info("Order placed successfully: {}", order.getOrderId());

        return mapToOrderResponse(order);
    }

    private void validateOrder(OrderRequest request, User user, Stock stock){
        //validating type and price
        if(request.getOrderType() == OrderType.LIMIT && request.getPrice() == null){
            throw new OrderValidationException("Price is required for LIMIT orders");
        }

        if ((request.getOrderType() == OrderType.SL || request.getOrderType() == OrderType.SL_M)
                && request.getTriggerPrice() == null) {
            throw new OrderValidationException("Trigger price is required for Stop Loss orders");
        }

        // Checking funds for buy orders
        if(request.getTransactionType() == TransactionType.BUY){
            BigDecimal requiredFunds = calculateRequiredFunds(request, stock);
            if(user.getFreeMargin().compareTo(requiredFunds) < 0){
                throw new InsufficientFundsException(
                        String.format("Insufficient funds. Required: ₹%s, Available: ₹%s",
                                requiredFunds, user.getFreeMargin()));
            }
        }

        // Check holdings for sell orders
        if (request.getTransactionType() == TransactionType.SELL) {
            Integer availableQuantity = portfolioService.getAvailableQuantity(user, stock);
            if (availableQuantity < request.getQuantity()) {
                throw new OrderValidationException(
                        String.format("Insufficient stocks to sell. Available: %d, Requested: %d",
                                availableQuantity, request.getQuantity()));
            }
        }

        // Validate stock is active
        if (!stock.isActive()) {
            throw new OrderValidationException("Stock is not available for trading");
        }
    }

    private BigDecimal calculateRequiredFunds(OrderRequest request, Stock stock){
        BigDecimal price = request.getOrderType() == OrderType.MARKET
                ? stock.getCurrentPrice() : request.getPrice();

        if(price == null){
            throw new OrderValidationException("Unable to determine stock price");
        }
        return price.multiply(new BigDecimal(request.getQuantity()));
    }

    private String generateOrderId(){
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }

    private void processOrder(Order order){
        try{
            if(order.getOrderType() == OrderType.MARKET){
                // Execute market order immediately
                executeMarketOrder(order);
            }
            else{
                order.setStatus(OrderStatus.OPEN);
                orderRepository.save(order);

                // Block margin for the order
                blockMarginForOrder(order);
                log.info("Order {} placed in order book", order.getOrderId());
            }
        }
        catch (Exception e) {
            log.error("Error processing order: {}", order.getOrderId(), e);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason("Order processing failed: " + e.getMessage());
            orderRepository.save(order);
        }
    }

    @Transactional
    private void executeMarketOrder(Order order){
        try{
            //get current market price
            BigDecimal executionPrice = order.getStock().getCurrentPrice();
            if (executionPrice == null) {
                throw new OrderValidationException("Market price not available");
            }

            //set executed price
            order.setExecutedPrice(executionPrice);
            order.setExecutedQuantity(order.getQuantity());
            order.setStatus(OrderStatus.COMPLETE);
            order.setExecutedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Update portfolio
            portfolioService.updatePortfolioOnExecution(
                    order.getUser(),
                    order.getStock(),
                    order.getQuantity(),
                    executionPrice,
                    order.getTransactionType().name()
            );

            // Update user balance
            updateUserBalanceOnExecution(order);

            // Send execution notification email
            emailService.sendOrderExecutionEmail(
                    order.getUser().getEmail(),
                    order.getUser().getUsername(),
                    order.getOrderId(),
                    order.getStock().getSymbol(),
                    executionPrice.toString()
            );

            log.info("Market order executed: {} at price {}", order.getOrderId(), executionPrice);
        }
        catch(Exception e){
            log.error("Failed to execute market order: {}", order.getOrderId(), e);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason("Execution failed: " + e.getMessage());
            orderRepository.save(order);
        }
    }

    private void updateUserBalanceOnExecution(Order order){
        User user = order.getUser();
        BigDecimal executionValue = order.getExecutedPrice().multiply(new BigDecimal(order.getExecutedQuantity()));
        if(order.getTransactionType() == TransactionType.BUY){
            // Deduct money for buy orders
            user.setAvailableBalance(user.getAvailableBalance().subtract(executionValue));
        }
        else{
            // Add money for sell orders
            user.setAvailableBalance(user.getAvailableBalance().add(executionValue));
        }
        userService.updateBalance(user.getUsername(), BigDecimal.ZERO);
    }

    private void blockMarginForOrder(Order order){
        if(order.getTransactionType() == TransactionType.BUY){
            BigDecimal requiredMargin = calculateRequiredFunds(
                    OrderRequest.builder()
                            .orderType(order.getOrderType())
                            .quantity(order.getQuantity())
                            .price(order.getPrice())
                            .build(),
                    order.getStock());
            User user = order.getUser();
            user.setUsedMargin(user.getUsedMargin().add(requiredMargin));
            userService.updateBalance(user.getUsername(), BigDecimal.ZERO);
        }
    }

    public List<OrderResponse> getUserOrders(String username) {
        User user = userService.findByUsername(username);
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    public Page<OrderResponse> getUserOrdersPaginated(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return orders.map(this::mapToOrderResponse);
    }

    public OrderResponse getOrderById(String orderId, String username) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TradingPlatformException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new TradingPlatformException("Access denied");
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId, String username) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TradingPlatformException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new TradingPlatformException("Access denied");
        }

        if (order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.PENDING) {
            throw new TradingPlatformException("Cannot cancel order in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Release blocked margin
        releaseBlockedMargin(order);

        log.info("Order cancelled: {}", orderId);

        return mapToOrderResponse(order);
    }

    private void releaseBlockedMargin(Order order) {
        if (order.getTransactionType() == TransactionType.BUY && order.getStatus() == OrderStatus.CANCELLED) {
            BigDecimal blockedMargin = calculateRequiredFunds(
                    OrderRequest.builder()
                            .orderType(order.getOrderType())
                            .quantity(order.getQuantity())
                            .price(order.getPrice())
                            .build(),
                    order.getStock()
            );

            User user = order.getUser();
            user.setUsedMargin(user.getUsedMargin().subtract(blockedMargin).max(BigDecimal.ZERO));
            userService.updateBalance(user.getUsername(), BigDecimal.ZERO); // Save user
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = OrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .kiteOrderId(order.getKiteOrderId())
                .stockSymbol(order.getStock().getSymbol())
                .stockName(order.getStock().getCompanyName())
                .transactionType(order.getTransactionType())
                .orderType(order.getOrderType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .executedPrice(order.getExecutedPrice())
                .executedQuantity(order.getExecutedQuantity())
                .status(order.getStatus())
                .validity(order.getValidity())
                .disclosedQuantity(order.getDisclosedQuantity())
                .triggerPrice(order.getTriggerPrice())
                .notes(order.getNotes())
                .rejectionReason(order.getRejectionReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .executedAt(order.getExecutedAt())
                .build();

        // Calculating derived fields
        if (order.getPrice() != null) {
            response.setTotalValue(order.getPrice().multiply(new BigDecimal(order.getQuantity())));
        }

        if (order.getExecutedPrice() != null && order.getExecutedQuantity() != null) {
            response.setExecutedValue(order.getExecutedPrice().multiply(new BigDecimal(order.getExecutedQuantity())));
            response.setRemainingQuantity(order.getQuantity() - order.getExecutedQuantity());
        }

        return response;
    }



}
