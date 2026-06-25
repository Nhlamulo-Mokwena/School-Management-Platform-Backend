package com.school.application.platform.backend.dto;

import com.school.application.platform.backend.entities.Application.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ApplicationResponse is what we send back to the frontend.
// We never return the raw entity — it may contain sensitive data
// or lazy-loaded relationships that would cause serialization errors.
// This record contains exactly what the frontend needs and nothing more.
public record ApplicationResponse(
        Long              id,
        String            referenceNumber,

        // Child
        String            childFirstName,
        String            childLastName,
        LocalDate         childDob,
        String            childIdNumber,
        String            gradeApplying,

        // Parent
        String            parentFirstName,
        String            parentLastName,
        String            parentEmail,
        String            parentPhone,
        String            relationship,
        String            address,

        // School
        String            schoolName,

        // Documents — paths so the frontend can display/download them
        String            birthCertificatePath,
        String            parentIdPath,
        String            reportCardPath,

        // Status & admin feedback
        ApplicationStatus status,
        String            adminNotes,

        // Timestamps
        LocalDateTime     submittedAt,
        LocalDateTime     updatedAt
) {}