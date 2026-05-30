package com.pg.controller;

import com.pg.model.User;
import com.pg.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder  = encoder;
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userRepo.findAll();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        String username = str(req.get("username"));
        if (username.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
        if (userRepo.existsByUsername(username))
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));

        String password = str(req.get("password"));
        if (password.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));

        User u = new User();
        u.setName(str(req.get("name")));
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setEmail(str(req.get("email")));
        u.setPhone(str(req.get("phone")));
        u.setActive(true);

        String roleStr = str(req.getOrDefault("role", "USER"));
        try { u.setRole(User.Role.valueOf(roleStr.toUpperCase())); }
        catch (IllegalArgumentException e) { u.setRole(User.Role.USER); }

        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());

        User saved = userRepo.save(u);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> req) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (req.containsKey("name"))  u.setName(str(req.get("name")));
        if (req.containsKey("email")) u.setEmail(str(req.get("email")));
        if (req.containsKey("phone")) u.setPhone(str(req.get("phone")));

        if (req.containsKey("role")) {
            try { u.setRole(User.Role.valueOf(str(req.get("role")).toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }

        if (req.containsKey("password")) {
            String pw = str(req.get("password"));
            if (!pw.isEmpty()) u.setPassword(encoder.encode(pw));
        }

        u.setUpdatedAt(LocalDateTime.now());
        User saved = userRepo.save(u);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
