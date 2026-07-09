package com.github.antorof1.flightreservationservice.security;

import com.github.antorof1.flightreservationservice.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HandlerExceptionResolver resolver;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtils, resolver);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Requests without an Authorization header should pass through unauthenticated")
    void noAuthorizationHeader_PassesThroughUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/flights");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(resolver);
    }

    @Test
    @DisplayName("A non-Bearer Authorization header should pass through unauthenticated")
    void nonBearerAuthorizationHeader_PassesThroughUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/flights");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("A valid token should populate the SecurityContext with a JwtPrincipal and continue the chain")
    void validToken_PopulatesSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reservations/1");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mockClaims("1", "ADMIN");
        when(jwtUtils.extractClaims("valid-token")).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(new JwtPrincipal(1L, UserRole.ADMIN));
        assertThat(authentication.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_ADMIN");
        verifyNoInteractions(resolver);
    }

    @Test
    @DisplayName("A token missing the subject claim should be delegated to the exception resolver")
    void tokenMissingSubject_DelegatesToResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reservations/1");
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mockClaims(null, "ADMIN");
        when(jwtUtils.extractClaims("bad-token")).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        verify(resolver).resolveException(eq(request), eq(response), eq(null), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("A token missing the role claim should be delegated to the exception resolver")
    void tokenMissingRole_DelegatesToResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reservations/1");
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mockClaims("1", null);
        when(jwtUtils.extractClaims("bad-token")).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        verify(resolver).resolveException(eq(request), eq(response), eq(null), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("An unparseable token should be delegated to the exception resolver instead of propagating")
    void unparseableToken_DelegatesToResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reservations/1");
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ExpiredJwtException exception = new ExpiredJwtException(null, null, "expired");
        when(jwtUtils.extractClaims("expired-token")).thenThrow(exception);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        verify(resolver).resolveException(request, response, null, exception);
    }

    @Test
    @DisplayName("shouldNotFilter() should skip auth endpoints and apply to everything else")
    void shouldNotFilter_OnlySkipsAuthEndpoints() throws Exception {
        MockHttpServletRequest authRequest = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        authRequest.setServletPath("/api/v1/auth/login");

        MockHttpServletRequest securedRequest = new MockHttpServletRequest("GET", "/api/v1/reservations/1");
        securedRequest.setServletPath("/api/v1/reservations/1");

        assertThat(filter.shouldNotFilter(authRequest)).isTrue();
        assertThat(filter.shouldNotFilter(securedRequest)).isFalse();
    }

    private Claims mockClaims(String subject, String role) {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn(subject);
        when(claims.get("role", String.class)).thenReturn(role);
        return claims;
    }
}
