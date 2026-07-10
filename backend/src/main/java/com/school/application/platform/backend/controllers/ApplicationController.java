package com.school.application.platform.backend.controllers;

import com.school.application.platform.backend.dto.ApplicationRequest;
import com.school.application.platform.backend.dto.ApplicationResponse;
import com.school.application.platform.backend.dto.StatusUpdateRequest;
import com.school.application.platform.backend.entities.Application;
import com.school.application.platform.backend.entities.Application.ApplicationStatus;
import com.school.application.platform.backend.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
// @CrossOrigin removed — CORS is configured in SecurityConfig.corsConfigurationSource()
// Keeping @CrossOrigin here alongside Security-level CORS can cause duplicate headers
// which breaks requests instead of fixing them.
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ── PARENT endpoints ──────────────────────────────────────────

    @PostMapping(value = "/submit", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApplicationResponse> submit(
            @RequestPart("data")             @Valid ApplicationRequest request,
            @RequestPart("birthCertificate")       MultipartFile birthCertificate,
            @RequestPart("parentId")               MultipartFile parentId,
            @RequestPart("reportCard")             MultipartFile reportCard,
            Authentication auth
    ) {
        ApplicationResponse response = applicationService.submit(
                request, birthCertificate, parentId, reportCard, auth.getName()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication auth) {
        return ResponseEntity.ok(applicationService.getMyApplications(auth.getName()));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApplicationResponse> getMyApplication(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(applicationService.getMyApplication(id, auth.getName()));
    }

    @DeleteMapping("/my/{id}/withdraw")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> withdraw(
            @PathVariable Long id,
            Authentication auth
    ) {
        applicationService.withdraw(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // ── ADMIN endpoints ───────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/admin/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getByStatus(
            @RequestParam ApplicationStatus status
    ) {
        return ResponseEntity.ok(applicationService.getByStatus(status));
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid StatusUpdateRequest request
    ) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationService.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(applicationService.getDashboardStats());
    }

    // ── Exception handlers ────────────────────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    // Add this method inside ApplicationController.java
// alongside the existing parent and admin endpoints.

    // GET /api/applications/teacher
// Returns all applications — teacher can see everything but cannot approve/decline.
// In a future version this can be filtered to only show applications
// for the teacher's assigned school.
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForTeacher() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    // PATCH /api/applications/teacher/{id}/review
// Teacher marks an application as UNDER_REVIEW and adds a recommendation note.
// This signals to the admin that the teacher has looked at it.
//    @PatchMapping("/teacher/{id}/review")
//    @PreAuthorize("hasRole('TEACHER')")
//    public ResponseEntity<ApplicationResponse> markUnderReview(
//            @PathVariable Long id,
//            @RequestBody @Valid StatusUpdateRequest request
//    ) {
//        // Only UNDER_REVIEW is valid from a teacher — they cannot ACCEPT or DECLINE
//        if (request.status() != Application.ApplicationStatus.UNDER_REVIEW) {
//            throw new IllegalArgumentException("Teachers can only set status to UNDER_REVIEW");
//        }
//        return ResponseEntity.ok(applicationService.updateStatus(id, request));
//    }

    @PatchMapping("/teacher/{id}/review")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApplicationResponse> markUnderReview(
            @PathVariable Long id,
            @RequestBody @Valid StatusUpdateRequest request
    ) {
        return ResponseEntity.ok(applicationService.markUnderReview(id, request));
    }
}