package com.warehouse.warehouse_management.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtil {

    private static final String SECRET = "warehouse-management-secret-key-2026";
    private static final long EXPIRATION_MS = 86_400_000L;
    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {
    }

    public static String generateToken(String subject) {
        Date now = new Date();

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(SECRET_KEY)
                .compact();
    }
}
