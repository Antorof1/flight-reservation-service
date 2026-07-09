package com.github.antorof1.flightreservationservice.auth;

import com.github.antorof1.flightreservationservice.AbstractControllerTest;
import com.github.antorof1.flightreservationservice.auth.dto.AuthResponse;
import com.github.antorof1.flightreservationservice.auth.dto.LoginRequest;
import com.github.antorof1.flightreservationservice.auth.dto.RegisterRequest;
import com.github.antorof1.flightreservationservice.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends AbstractControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/register should create an account and return 201")
    void shouldRegister() throws Exception {
        RegisterRequest request = new RegisterRequest("john.doe@example.com", "John Doe", "password123");
        AuthResponse response = new AuthResponse("token", request.email(), request.name());

        when(authService.register(request.email(), request.name(), request.password())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("token"))
            .andExpect(jsonPath("$.email").value(request.email()))
            .andExpect(jsonPath("$.name").value(request.name()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 when the email is malformed")
    void shouldRejectRegisterWithInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "John Doe", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details[0]").value("email: Invalid email format"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 when the password is too short")
    void shouldRejectRegisterWithShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("john.doe@example.com", "John Doe", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("password: Password must be at least 8 characters long"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 when required fields are blank")
    void shouldRejectRegisterWithBlankFields() throws Exception {
        RegisterRequest request = new RegisterRequest("", "", "");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.length()").value(4));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 409 when the email is already registered")
    void shouldRejectRegisterWithDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("john.doe@example.com", "John Doe", "password123");

        when(authService.register(request.email(), request.name(), request.password()))
            .thenThrow(new DuplicateResourceException("Email is already in use: " + request.email()));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email is already in use: " + request.email()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should authenticate and return 200")
    void shouldLogin() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", "password123");
        AuthResponse response = new AuthResponse("token", request.email(), "John Doe");

        when(authService.login(request.email(), request.password())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token"))
            .andExpect(jsonPath("$.email").value(request.email()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 400 when the email is blank")
    void shouldRejectLoginWithBlankEmail() throws Exception {
        LoginRequest request = new LoginRequest("", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("email: Email is required"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/** should be reachable without an Authorization header")
    void authEndpointsShouldNotRequireAuthentication() throws Exception {
        when(authService.login(anyString(), anyString()))
            .thenReturn(new AuthResponse("token", "john.doe@example.com", "John Doe"));

        LoginRequest request = new LoginRequest("john.doe@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}
