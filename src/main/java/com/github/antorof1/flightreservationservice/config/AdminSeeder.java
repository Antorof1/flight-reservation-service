package com.github.antorof1.flightreservationservice.config;

import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRepository;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedAdminEmail;
    private final String seedAdminPassword;

    public AdminSeeder(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${app.seed-admin.email:}") String seedAdminEmail,
                        @Value("${app.seed-admin.password:}") String seedAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedAdminEmail = seedAdminEmail;
        this.seedAdminPassword = seedAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (seedAdminPassword.isBlank() || userRepository.existsByEmail(seedAdminEmail)) {
            return;
        }

        userRepository.save(new User(
            seedAdminEmail,
            "Demo Admin",
            passwordEncoder.encode(seedAdminPassword),
            UserRole.ADMIN
        ));
    }
}
