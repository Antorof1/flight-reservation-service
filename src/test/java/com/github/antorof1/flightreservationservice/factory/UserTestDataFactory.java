package com.github.antorof1.flightreservationservice.factory;

import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserTestDataFactory {
    @Autowired
    private UserService userService;

    public User createUser(String email, String name) {
        User user = new User(email, name);

        return userService.createUser(user);
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
