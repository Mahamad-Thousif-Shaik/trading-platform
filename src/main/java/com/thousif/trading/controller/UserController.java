package com.thousif.trading.controller;

import com.thousif.trading.entity.Stock;
import com.thousif.trading.entity.User;
import com.thousif.trading.service.auth.UserService;
import com.thousif.trading.service.external.AlphaVantageService;
import com.thousif.trading.service.trading.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final StockService service;

    @GetMapping("/profile")
    public ResponseEntity<Stock> getProfile(Authentication authentication){
//        User user = userService.findByUsername(authentication.getName());
//        user.setPassword(null);
        long before = System.currentTimeMillis();
        Stock res = service.getStockBySymbol("TCS");
        long after = System.currentTimeMillis();
        log.info("{} ",after-before);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication){
        Map<String, Object> balance = userService.getUserBalance(authentication);
        return ResponseEntity.ok(balance);
    }


}
