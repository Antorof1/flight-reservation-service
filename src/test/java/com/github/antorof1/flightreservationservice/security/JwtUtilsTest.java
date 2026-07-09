package com.github.antorof1.flightreservationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {
    private static final String SECRET_KEY = "ZGtsYWhmbGtqZGxubmRqa2xza2xmZm5rbGRqc2JmbGQ=";

    private final JwtUtils jwtUtils = new JwtUtils(SECRET_KEY);

    @Test
    @DisplayName("generateToken() should produce a token whose claims round-trip through extractClaims()")
    void generateAndExtractClaims_RoundTrips() {
        String token = jwtUtils.generateToken("42", Map.of("role", "ADMIN"));

        Claims claims = jwtUtils.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("generateToken() should set an expiration roughly one hour after issuance")
    void generateToken_SetsOneHourExpiration() {
        String token = jwtUtils.generateToken("42", null);

        Claims claims = jwtUtils.extractClaims(token);

        long validityMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(Duration.ofMillis(validityMillis)).isCloseTo(Duration.ofHours(1), Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("generateToken() should tolerate a null claims map")
    void generateToken_NullClaims_DoesNotFail() {
        String token = jwtUtils.generateToken("42", null);

        Claims claims = jwtUtils.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
    }

    @Test
    @DisplayName("extractClaims() should throw ExpiredJwtException for an expired token")
    void extractClaims_ExpiredToken_ThrowsException() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        String expiredToken = Jwts.builder()
            .subject("42")
            .issuedAt(Date.from(Instant.now().minus(Duration.ofHours(2))))
            .expiration(Date.from(Instant.now().minus(Duration.ofHours(1))))
            .signWith(key)
            .compact();

        assertThatThrownBy(() -> jwtUtils.extractClaims(expiredToken))
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("extractClaims() should throw SignatureException when the token was signed with a different key")
    void extractClaims_WrongSignature_ThrowsException() {
        SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("YW5vdGhlcmtleXRoYXRpc2xvbmdlbm91Z2h0b2JlYXZhbGlka2V5"));
        String tokenSignedWithOtherKey = Jwts.builder()
            .subject("42")
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
            .signWith(otherKey)
            .compact();

        assertThatThrownBy(() -> jwtUtils.extractClaims(tokenSignedWithOtherKey))
            .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("extractClaims() should throw MalformedJwtException for a garbage token")
    void extractClaims_MalformedToken_ThrowsException() {
        assertThatThrownBy(() -> jwtUtils.extractClaims("not-a-valid-jwt"))
            .isInstanceOf(MalformedJwtException.class);
    }
}
