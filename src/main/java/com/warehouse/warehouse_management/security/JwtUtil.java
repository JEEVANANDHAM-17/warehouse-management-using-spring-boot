package com.warehouse.warehouse_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET =
            "warehouseSecretKeywarehouseSecretKeywarehouseSecretKey";

    private static final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    public static void validateToken(String token) {
        extractClaims(token);
    }

    public static String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    private static Claims extractClaims(String token) {
        String jwt = normalizeToken(token);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    private static String normalizeToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("JWT token cannot be null");
        }

        String jwt = token.trim();

        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7).trim();
        }

        return jwt;
    }
}
