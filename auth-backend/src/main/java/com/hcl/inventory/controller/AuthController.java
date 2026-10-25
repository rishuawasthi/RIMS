package com.hcl.inventory.controller;

import com.hcl.inventory.config.JwtUtil;
import com.hcl.inventory.dto.AuthResponse;
import com.hcl.inventory.dto.LoginRequest;
import com.hcl.inventory.entity.AppUser;
import com.hcl.inventory.enums.UserStatus;
import com.hcl.inventory.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * Authentication controller — issues JWT tokens.
 *
 * <p>POST /auth/login is the only public endpoint in the application.
 * All other endpoints require a valid Bearer token.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Issue and validate JWT tokens")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate with username + password and receive a JWT.
     *
     * <ul>
     *   <li>Returns 200 + {@link AuthResponse} on success.</li>
     *   <li>Returns 401 when credentials are wrong or account is inactive/locked.</li>
     * </ul>
     */
    @Operation(summary = "Login", description = "Provide username and password to receive a JWT access token.")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Optional<AppUser> userOpt = userRepository
                .findByUsernameIgnoreCase(request.getUsername().trim());

        if (userOpt.isEmpty()) {
            return unauthorized("Invalid username or password.");
        }

        AppUser user = userOpt.get();

        // Reject locked / inactive accounts before checking the password
        if (user.getStatus() == UserStatus.INACTIVE) {
            return unauthorized("Account is inactive. Contact an administrator.");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            return unauthorized("Account is locked. Contact an administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return unauthorized("Invalid username or password.");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        AuthResponse response = new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                user.getFullName()
        );

        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------
    private ResponseEntity<Map<String, String>> unauthorized(String message) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", message));
    }
}
