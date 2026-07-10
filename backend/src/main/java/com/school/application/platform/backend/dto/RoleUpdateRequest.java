package com.school.application.platform.backend.dto;

import jakarta.validation.constraints.NotBlank;

// Used when admin changes a user's role.
// Frontend sends "PARENT", "TEACHER", or "ADMIN" — controller adds the ROLE_ prefix,
// matching the same pattern used at registration.
public record RoleUpdateRequest(
        @NotBlank String role
) {}