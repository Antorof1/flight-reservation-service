package com.github.antorof1.flightreservationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final SecretKey secretKey;

    public JwtUtils(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String generateToken(String sub, @Nullable Map<String, Object> claims) {
        Date expiration = Date.from(Instant.now().plus(TOKEN_VALIDITY));

        return Jwts.builder()
            .subject(sub)
            .claims(claims != null ? claims : Map.of())
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(secretKey)
            .compact();
    }
}
