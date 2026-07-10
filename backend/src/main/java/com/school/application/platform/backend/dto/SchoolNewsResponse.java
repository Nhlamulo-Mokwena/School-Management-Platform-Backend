package com.school.application.platform.backend.dto;

import java.time.LocalDateTime;

// What we send back — same shape for both public and admin responses.
// The frontend uses published flag to show draft/live status on the admin page.
public record SchoolNewsResponse(
        Long          id,
        String        title,
        String        content,
        String        summary,
        String        category,
        String        postedBy,
        boolean       published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}