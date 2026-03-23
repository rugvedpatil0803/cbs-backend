package com.project.cbsbackend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey123456";
    private final long ACCESS_EXPIRATION  = 1000 * 60 * 60;            // 1 hour
    private final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7;  // 7 days

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(Long userId, String email, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(getSignKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String email, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getSignKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()                  // ← parserBuilder() removed in 0.12+
                .verifyWith(getSignKey())      // ← replaces setSigningKey()
                .build()
                .parseSignedClaims(token)      // ← replaces parseClaimsJws()
                .getPayload();                 // ← replaces getBody()
    }

    public String extractEmail(String token)       { return extractAllClaims(token).getSubject(); }
    public Long extractUserId(String token)         { return extractAllClaims(token).get("userId", Long.class); }
    public List<String> extractRoles(String token)  { return extractAllClaims(token).get("roles", List.class); }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(extractAllClaims(token).get("type", String.class));
    }
}