package com.school.application.platform.backend.dto;

import com.school.application.platform.backend.entities.Application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

// Used by the admin when approving or declining an application.
// adminNotes is optional — the admin may or may not leave a message.
public record StatusUpdateRequest(
        @NotNull ApplicationStatus status,   // ACCEPTED or DECLINED
        String adminNotes                    // optional message to the parent
) {}