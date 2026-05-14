package com.carcaddy.controller;

import com.carcaddy.dto.*;
import com.carcaddy.entity.AppUser;
import com.carcaddy.entity.Customer;
import com.carcaddy.repository.AppUserRepository;
import com.carcaddy.repository.CustomerRepository;
import com.carcaddy.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username already exists"));
        }

        // For customer role, validate required customer fields upfront
        if ("ROLE_CUSTOMER".equals(request.getRole())) {
            if (request.getCustomerName() == null || request.getCustomerName().isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Full name is required for customer registration"));
            if (request.getEmailId() == null || request.getEmailId().isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required for customer registration"));
            if (request.getContactNumber() == null || request.getContactNumber().isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Contact number is required for customer registration"));
            if (request.getDrivingLicense() == null || request.getDrivingLicense().isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Driving license is required for customer registration"));
            if (request.getAddress() == null || request.getAddress().isBlank())
                return ResponseEntity.badRequest().body(Map.of("message", "Address is required for customer registration"));

            if (customerRepository.findByEmailId(request.getEmailId()).isPresent())
                return ResponseEntity.badRequest().body(Map.of("message", "A customer with this email already exists"));
            if (customerRepository.findByDrivingLicense(request.getDrivingLicense()).isPresent())
                return ResponseEntity.badRequest().body(Map.of("message", "A customer with this driving license already exists"));
        }

        // Create AppUser
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setSecurityQuestion(request.getSecurityQuestion());
        user.setSecurityAnswer(request.getSecurityAnswer() != null
                ? request.getSecurityAnswer().toLowerCase() : null);

        AppUser saved = userRepository.save(user);
        log.info("User registered: {}", saved.getUsername());

        // Auto-create and link Customer profile for ROLE_CUSTOMER
        if ("ROLE_CUSTOMER".equals(request.getRole())) {
            long count = customerRepository.count() + 1;
            String customerId = String.format("CUST-%03d", count);

            Customer customer = new Customer();
            customer.setCustomerId(customerId);
            customer.setCustomerName(request.getCustomerName());
            customer.setEmailId(request.getEmailId());
            customer.setContactNumber(request.getContactNumber());
            customer.setDrivingLicense(request.getDrivingLicense());
            customer.setAddress(request.getAddress());
            customer.setOccupation(request.getOccupation());
            customer.setLoyaltyPoints(0);
            customer.setBlacklisted(false);
            customer.setAppUser(saved); // link to AppUser

            customerRepository.save(customer);
            log.info("Customer profile auto-created and linked | customerId={}, username={}", customerId, saved.getUsername());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());

        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        log.info("Login successful for: {}", user.getUsername());

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole(), "Login successful"));
    }

    // Change password (authenticated)
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String username = getCurrentUsername();
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // Update username
    @PutMapping("/update-username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> body) {
        String username = getCurrentUsername();
        String newUsername = body.get("newUsername");

        if (userRepository.existsByUsername(newUsername)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken"));
        }

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(newUsername);
        userRepository.save(user);

        // Generate new token with updated username
        String newToken = jwtUtil.generateToken(newUsername, user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Username updated successfully",
                "token", newToken,
                "username", newUsername
        ));
    }

    // Set/update security question
    @PutMapping("/security-question")
    public ResponseEntity<?> setSecurityQuestion(@RequestBody Map<String, String> body) {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setSecurityQuestion(body.get("securityQuestion"));
        user.setSecurityAnswer(body.get("securityAnswer").toLowerCase());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Security question updated successfully"));
    }

    // Get own security question
    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion() {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of("securityQuestion", user.getSecurityQuestion()));
    }

    // Get security question by username (for forgot password)
    @GetMapping("/security-question/{username}")
    public ResponseEntity<?> getSecurityQuestionByUsername(@PathVariable String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null || user.getSecurityQuestion() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("securityQuestion", user.getSecurityQuestion()));
    }

    // Reset password via security question
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String securityAnswer = body.get("securityAnswer");
        String newPassword = body.get("newPassword");

        AppUser user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!securityAnswer.toLowerCase().equals(user.getSecurityAnswer())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Security answer is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // Verify security answer
    @PostMapping("/verify-security-answer")
    public ResponseEntity<?> verifySecurityAnswer(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String answer = body.get("securityAnswer");

        AppUser user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        boolean correct = answer.toLowerCase().equals(user.getSecurityAnswer());
        return ResponseEntity.ok(Map.of("correct", correct));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
