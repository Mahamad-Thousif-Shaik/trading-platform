package com.thousif.trading.controller;

import com.thousif.trading.entity.User;
import com.thousif.trading.service.auth.UserService;
import com.thousif.trading.service.external.AlphaVantageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AlphaVantageService service;

    @GetMapping("/profile")
    public ResponseEntity<Map<?,?>> getProfile(Authentication authentication){
//        User user = userService.findByUsername(authentication.getName());
//        user.setPassword(null);
        Map<String, Object> res = service.getHistoricalData("AAPL");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication){
        Map<String, Object> balance = userService.getUserBalance(authentication);
        return ResponseEntity.ok(balance);
    }


}
