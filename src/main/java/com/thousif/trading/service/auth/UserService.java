package com.thousif.trading.service.auth;

import com.thousif.trading.entity.User;
import com.thousif.trading.exception.TradingPlatformException;
import com.thousif.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByUsernameOrEmail(String identifier){
        return userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new TradingPlatformException("User not found: " + identifier));
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new TradingPlatformException("User not found:" + username));
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new TradingPlatformException("User not found:" + email));
    }

    public Map<String, Object> getUserBalance(Authentication authentication){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new TradingPlatformException("User not found"));
        return Map.of(
                "availableBalance", user.getAvailableBalance(),
                "usedMargin", user.getUsedMargin()
        );
    }

    public User updateBalance(String username, BigDecimal amount) {
        User user = findByUsername(username);
        user.setAvailableBalance(user.getAvailableBalance().add(amount));
        return userRepository.save(user);
    }

}
