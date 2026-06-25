package com.school.application.platform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// ApplicationRequest is the data the frontend sends when submitting a new application.
// It is a plain record — no JPA annotations, no database concern.
// The service maps this into an Application entity before saving.
public record ApplicationRequest(

        // ── Child details ─────────────────────────────────────────────
        @NotBlank String childFirstName,
        @NotBlank String childLastName,

        @NotNull  LocalDate childDob,

        @NotBlank @Size(min = 13, max = 13, message = "SA ID number must be 13 digits")
        String childIdNumber,

        @NotBlank String gradeApplying,

        // ── Parent contact ────────────────────────────────────────────
        @NotBlank String parentFirstName,
        @NotBlank String parentLastName,
        @NotBlank String parentEmail,
        @NotBlank String parentPhone,
        @NotBlank String relationship,
        @NotBlank String address,

        // ── School ────────────────────────────────────────────────────
        String schoolName

        // Note: document file paths are NOT part of this request.
        // Files are uploaded separately via multipart/form-data and
        // their paths are passed directly to the service method.
) {}