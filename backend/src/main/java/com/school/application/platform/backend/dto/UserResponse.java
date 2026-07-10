package com.school.application.platform.backend.dto;

// What we send back to the frontend when listing users.
// Never return the password hash — even hashed, it should never leave the server.
public record UserResponse(
        Long   id,
        String email,
        String role
) {}