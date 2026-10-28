package com.hcl.inventory.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utility bean for creating and validating JWT access tokens.
 * The secret key and TTL are externalised to application.properties.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${rims.jwt.secret}") String secret,
            @Value("${rims.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalArgumentException(
                    "JWT signing secret ('rims.jwt.secret') must be at least 32 characters (256 bits) long for secure HS256 signing."
            );
        }
        // Ensure the key is at least 256 bits (32 bytes) for HS256
        byte[] keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a signed JWT for the given username and role.
     */
    public String generateToken(String username, String role) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(Map.of("role", role))
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extract the username (subject) from a token.
     * Throws JwtException if the token is invalid or expired.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract the role claim from a token, inspecting "role", "roles", and "authorities".
     */
    public String extractRole(String token) {
        Claims claims = parseClaims(token);
        Object roleObj = claims.get("role");
        if (roleObj == null) {
            roleObj = claims.get("roles");
        }
        if (roleObj == null) {
            roleObj = claims.get("authorities");
        }
        if (roleObj instanceof List<?> list && !list.isEmpty()) {
            return String.valueOf(list.get(0));
        }
        return roleObj != null ? String.valueOf(roleObj) : null;
    }

    /**
     * Return true when the token is syntactically valid and not expired.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // -----------------------------------------------------------------
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
