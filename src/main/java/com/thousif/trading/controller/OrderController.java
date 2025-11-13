package com.thousif.trading.controller;

import com.thousif.trading.dto.request.OrderRequest;
import com.thousif.trading.dto.response.OrderResponse;
import com.thousif.trading.service.trading.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request,
                                                    Authentication authentication) {
        OrderResponse response = orderService.placeOrder(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(Authentication authentication) {
        List<OrderResponse> orders = orderService.getUserOrders(authentication.getName());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<OrderResponse>> getUserOrdersPaginated(Authentication authentication,
                                                                      Pageable pageable) {
        Page<OrderResponse> orders = orderService.getUserOrdersPaginated(authentication.getName(), pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId,
                                                  Authentication authentication) {
        OrderResponse order = orderService.getOrderById(orderId, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId,
                                                     Authentication authentication) {
        OrderResponse response = orderService.cancelOrder(orderId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
