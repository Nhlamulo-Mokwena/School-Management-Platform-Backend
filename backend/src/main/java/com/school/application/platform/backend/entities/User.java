package com.school.application.platform.backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;  // always stored as a BCrypt hash, never plaintext

    @Column(nullable = false)
    private String role;      // "ROLE_ADMIN", "ROLE_TEACHER", or "ROLE_PARENT"
                            // Spring Security expects the "ROLE_" prefix
}