package com.github.antorof1.flightreservationservice;

import com.github.antorof1.flightreservationservice.config.SecurityConfig;
import com.github.antorof1.flightreservationservice.security.JwtPrincipal;
import com.github.antorof1.flightreservationservice.security.JwtUtils;
import com.github.antorof1.flightreservationservice.security.RestAuthenticationEntryPoint;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
public class AbstractControllerTest {
    @MockitoBean
    protected JwtUtils jwtUtils;

    @BeforeEach
    protected void setUpSecurity() {
        when(jwtUtils.extractClaims(anyString())).thenReturn(null);
    }

    protected UsernamePasswordAuthenticationToken mockAuth(Long userId, UserRole role) {
        JwtPrincipal principal = new JwtPrincipal(userId, role);

        List<UserRole> authorities = List.of(role);

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    protected UsernamePasswordAuthenticationToken mockAuth(User user) {
        return mockAuth(user.getId(), user.getRole());
    }

    protected UsernamePasswordAuthenticationToken mockUserAuth() {
        return mockAuth(1L, UserRole.USER);
    }

    protected UsernamePasswordAuthenticationToken mockAdminAuth() {
        return mockAuth(1L, UserRole.ADMIN);
    }
}
