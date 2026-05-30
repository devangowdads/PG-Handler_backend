package com.pg.config;

import com.pg.model.User;
import com.pg.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Always ensure admin exists with fixed credentials: admin / admin123
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            // Reset password every startup to guarantee admin123 always works
            User admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("✅ Admin password reset to: admin / admin123");
        } else {
            User admin = new User();
            admin.setName("Administrator");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@pgmanager.com");
            admin.setPhone("");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("✅ Admin user created: admin / admin123");
        }

        // Seed demo user (only if not present)
        if (!userRepository.existsByUsername("user1")) {
            User user = new User();
            user.setName("Demo User");
            user.setUsername("user1");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user1@pgmanager.com");
            user.setPhone("");
            user.setRole(User.Role.USER);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("✅ Demo user created: user1 / user123");
        }
    }
}
