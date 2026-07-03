package com.github.antorof1.flightreservationservice.factory;

import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRepository;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserTestDataFactory {
    private static final String DEFAULT_PASSWORD = "password123";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(String email, String name) {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        User user = new User(
            email,
            name,
            encodedPassword,
            UserRole.USER
        );

        return userRepository.save(user);
    }

    public User createUser(String email) {
        return createUser(email, "Test User");
    }

    @Transactional
    public List<User> createUsers(int count) {
        List<User> users = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String email = String.format("test-%d@example.com", i);
            String name = String.format("Test User %d", i);

            users.add(createUser(email, name));
        }

        return users;
    }
}
