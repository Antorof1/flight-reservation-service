package com.github.antorof1.flightreservationservice.user;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {
    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    @Override
    public @NonNull Collection<UserRole> getAuthorities() {
        return List.of(user.getRole());
    }

    @Override
    public @NonNull String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }
}
