package com.github.antorof1.flightreservationservice.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private static final String REQUEST_URI = "/api/v1/reservations/1";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @Test
    @DisplayName("ResourceNotFoundException maps to 404")
    void handlesResourceNotFoundException() {
        ResponseEntity<ApiErrorResponse> response =
            handler.handleResourceNotFoundException(new ResourceNotFoundException("Reservation not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Reservation not found");
        assertThat(response.getBody().path()).isEqualTo(REQUEST_URI);
    }

    @Test
    @DisplayName("SeatAlreadyHeldException and SeatUnavailableException map to 409")
    void handlesSeatConflictExceptions() {
        ResponseEntity<ApiErrorResponse> response =
            handler.handleSeatConflictExceptions(new SeatAlreadyHeldException("Seat is held"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("Seat is held");
    }

    @Test
    @DisplayName("InvalidReservationStateException maps to 400")
    void handlesInvalidReservationStateException() {
        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidReservationStateExceptions(
            new InvalidReservationStateException("Cannot confirm"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Cannot confirm");
    }

    @Test
    @DisplayName("ExpiredJwtException maps to 401 with a generic message that hides token details")
    void handlesExpiredJwtException() {
        ExpiredJwtException exception = new ExpiredJwtException(null, null, "JWT expired at ...");

        ResponseEntity<ApiErrorResponse> response = handler.handleExpiredJwtException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Token has expired");
        assertThat(response.getBody().path()).isEqualTo(REQUEST_URI);
    }

    @Test
    @DisplayName("SignatureException maps to 401 with a generic message")
    void handlesSignatureException() {
        SignatureException exception = new SignatureException("JWT signature does not match");

        ResponseEntity<ApiErrorResponse> response = handler.handleSignatureException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Invalid token signature");
    }

    @Test
    @DisplayName("MalformedJwtException maps to 401 with a generic message")
    void handlesMalformedJwtException() {
        MalformedJwtException exception = new MalformedJwtException("JWT payload is missing the subject");

        ResponseEntity<ApiErrorResponse> response = handler.handleMalformedJwtException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Malformed token structure");
    }

    @Test
    @DisplayName("AuthenticationException (e.g. a request with no credentials) maps to 401")
    void handlesAuthenticationException() {
        InsufficientAuthenticationException exception =
            new InsufficientAuthenticationException("Full authentication is required");

        ResponseEntity<ApiErrorResponse> response = handler.handleAuthenticationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Full authentication is required to access this resource");
    }

    @Test
    @DisplayName("DuplicateResourceException maps to 409")
    void handlesDuplicateResourceException() {
        DuplicateResourceException exception = new DuplicateResourceException("Email is already in use: a@b.com");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgumentException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("Email is already in use: a@b.com");
    }

    @Test
    @DisplayName("AuthorizationDeniedException maps to 403 without leaking the underlying reason")
    void handlesAuthorizationDeniedException() {
        AuthorizationDeniedException exception = new AuthorizationDeniedException("Access Denied");

        ResponseEntity<ApiErrorResponse> response = handler.handleAuthorizationDeniedException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Access denied");
    }

    @Test
    @DisplayName("Unhandled exceptions map to a generic 500 without leaking internal details")
    void handlesGenericException() {
        ResponseEntity<ApiErrorResponse> response =
            handler.handleGenericException(new RuntimeException("db connection refused"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}
