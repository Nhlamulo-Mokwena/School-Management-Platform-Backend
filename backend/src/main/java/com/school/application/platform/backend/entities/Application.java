package com.school.application.platform.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data  // Lombok generates all getters, setters, equals, hashCode, toString
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Who submitted this application ────────────────────────────
    // Many applications can belong to one parent user.
    // FetchType.LAZY means Hibernate won't load the full User object
    // unless you explicitly access it — better performance.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Child's details ───────────────────────────────────────────

    @Column(name = "child_first_name", nullable = false)
    private String childFirstName;

    @Column(name = "child_last_name", nullable = false)
    private String childLastName;

    // LocalDate is better than java.util.Date for date of birth —
    // it has no time or time zone attached, which avoids subtle bugs.
    @Column(name = "child_dob", nullable = false)
    private LocalDate childDob;

    // SA ID is always 13 digits — stored as String to preserve leading zeros.
    @Column(name = "child_id_number", nullable = false, length = 13)
    private String childIdNumber;

    @Column(name = "grade_applying", nullable = false)
    private String gradeApplying;  // e.g. "Grade 8"

    // ── Parent / guardian contact ─────────────────────────────────

    @Column(name = "parent_first_name", nullable = false)
    private String parentFirstName;

    @Column(name = "parent_last_name", nullable = false)
    private String parentLastName;

    @Column(name = "parent_email", nullable = false)
    private String parentEmail;

    @Column(name = "parent_phone", nullable = false)
    private String parentPhone;

    // e.g. "Mother", "Father", "Legal Guardian"
    @Column(name = "relationship", nullable = false)
    private String relationship;

    // TEXT allows longer addresses without a character limit
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    // ── School ────────────────────────────────────────────────────

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    // ── Document file paths ───────────────────────────────────────
    // We never store actual files in the database.
    // Files are uploaded to disk (or cloud storage like AWS S3),
    // and we only store the path/URL pointing to where they live.

    @Column(name = "birth_certificate_path")
    private String birthCertificatePath;

    @Column(name = "parent_id_path")
    private String parentIdPath;

    @Column(name = "report_card_path")
    private String reportCardPath;

    // ── Application status ────────────────────────────────────────
    // EnumType.STRING stores the name e.g. "PENDING" instead of the
    // ordinal number (0, 1, 2...). This is safer — if you ever reorder
    // the enum values, the stored data still makes sense.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;  // default on creation

    // Admin fills this when approving or declining
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // Human-readable reference shown to the parent e.g. "APP-2025-001"
    // Generated in the service layer before saving, must be unique.
    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    // ── Timestamps ────────────────────────────────────────────────
    // Hibernate sets these automatically — never set them manually.
    // updatable = false on submittedAt means it is set once and never changed.

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Status enum ───────────────────────────────────────────────
    // Inner enum keeps it close to the entity it belongs to.
    // These four states match exactly what the frontend displays.
    public enum ApplicationStatus {
        PENDING,       // submitted, not yet looked at
        UNDER_REVIEW,  // admin has opened and is reviewing it
        ACCEPTED,      // admin approved the application
        DECLINED       // admin rejected the application
    }
}