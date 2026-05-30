package com.pg.controller;

import com.pg.model.User;
import com.pg.repository.UserRepository;
import com.pg.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.get("username"), req.get("password")));
        String token = jwtUtil.generateToken(auth.getName());
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "username", user.getUsername(),
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "role", user.getRole().name()
                )
        ));
    }
}
