package com.school.application.platform.backend.controllers;

import com.school.application.platform.backend.entities.User;
import com.school.application.platform.backend.repositories.UserRepository;
import com.school.application.platform.backend.service.JwtUtilService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtilService jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager,
                          JwtUtilService jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password())); // hash before saving
        user.setRole("ROLE_" + req.role().toUpperCase());         // e.g. "PARENT" → "ROLE_PARENT"
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        // This line does the actual credential check — it calls loadUserByUsername,
        // then compares the provided password against the stored BCrypt hash.
        // If the credentials are wrong it throws an exception and returns 401.
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // Credentials passed — generate and return the JWT
        String token = jwtUtils.generateToken(req.email());
        return ResponseEntity.ok(Map.of("token", token, "type", "Bearer"));
    }

    // --- DTOs ---

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    // Role accepts "ADMIN", "TEACHER", or "PARENT" — we add the prefix in the method above
    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String role
    ) {}
}
