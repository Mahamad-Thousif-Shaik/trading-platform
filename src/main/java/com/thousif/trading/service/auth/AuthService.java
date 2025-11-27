package com.thousif.trading.service.auth;

import com.thousif.trading.dto.request.LoginRequest;
import com.thousif.trading.dto.request.RegisterRequest;
import com.thousif.trading.dto.response.AuthResponse;
import com.thousif.trading.entity.User;
import com.thousif.trading.enums.Role;
import com.thousif.trading.enums.UserStatus;
import com.thousif.trading.exception.TradingPlatformException;
import com.thousif.trading.repository.UserRepository;
import com.thousif.trading.security.JwtTokenProvider;
import com.thousif.trading.service.notification.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            throw new TradingPlatformException("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new TradingPlatformException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();

        user.setAvailableBalance(new BigDecimal("100000.00")); // Demo balance
        user = userRepository.save(user);

        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getUsername());

        log.info("User registered successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication.getName());

        User user = userRepository.findByUsernameOrEmail(request.getIdentifier())
                .orElseThrow(() -> new TradingPlatformException("User not found"));

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .message("Login successful")
                .build();
    }

}
