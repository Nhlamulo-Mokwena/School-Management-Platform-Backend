package com.school.application.platform.backend.dto;

import jakarta.validation.constraints.NotBlank;

// What the admin sends when creating or updating a news post.
public record SchoolNewsRequest(
        @NotBlank String title,
        @NotBlank String content,
        String  summary,   // optional — if blank the service auto-generates one from content
        @NotBlank String category,
        boolean published  // true = go live immediately, false = save as draft
) {}