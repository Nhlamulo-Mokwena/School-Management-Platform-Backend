package com.school.application.platform.backend.controllers;

import com.school.application.platform.backend.dto.RoleUpdateRequest;
import com.school.application.platform.backend.dto.UserResponse;
import com.school.application.platform.backend.entities.User;
import com.school.application.platform.backend.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/users
    // Admin-only — lists every registered user (parents, teachers, admins).
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole()))
                .toList();

        return ResponseEntity.ok(users);
    }

    // PATCH /api/users/{id}/role
    // Admin changes a user's role e.g. promote a parent to admin.
    // PATCH because we're updating one field, not the whole user.
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleUpdateRequest request,
            Authentication auth
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Safety check: prevent an admin from demoting themselves and
        // accidentally locking themselves out of the admin panel.
        if (user.getEmail().equals(auth.getName()) && !request.role().equalsIgnoreCase("ADMIN")) {
            throw new IllegalStateException("You cannot change your own admin role");
        }

        // Store with the ROLE_ prefix, same convention used at registration
        user.setRole("ROLE_" + request.role().toUpperCase());
        User saved = userRepository.save(user);

        return ResponseEntity.ok(new UserResponse(saved.getId(), saved.getEmail(), saved.getRole()));
    }

    // DELETE /api/users/{id}
    // Admin removes a user account entirely.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Prevent an admin from deleting their own account
        if (user.getEmail().equals(auth.getName())) {
            throw new IllegalStateException("You cannot delete your own account");
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // ── Exception handlers ────────────────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}