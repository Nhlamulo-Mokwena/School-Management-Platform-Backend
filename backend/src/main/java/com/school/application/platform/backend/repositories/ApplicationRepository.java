package com.school.application.platform.backend.repositories;

import com.school.application.platform.backend.entities.Application;
import com.school.application.platform.backend.entities.Application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// JpaRepository<Application, Long> gives us all the standard CRUD methods
// for free — save(), findById(), findAll(), delete(), count(), etc.
// We only need to define the custom queries specific to our app.
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // ── Parent queries ────────────────────────────────────────────
    // Spring Data JPA reads the method name and generates the SQL automatically.
    // "findBy" + "User_Id" translates to: WHERE user_id = ?

    // Get all applications submitted by a specific parent —
    // used on the parent's applications page to list their submissions.
    List<Application> findByUserId(Long userId);

    // Get a single application by ID but only if it belongs to the given user.
    // This prevents a parent from accessing another parent's application
    // by guessing the ID. Always use this instead of plain findById
    // when the request comes from a parent.
    Optional<Application> findByIdAndUserId(Long id, Long userId);

    // Get all applications for a parent filtered by status —
    // used when the parent filters their list by Pending, Accepted, etc.
    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    // ── Admin queries ─────────────────────────────────────────────

    // Get all applications with a specific status —
    // used on the admin dashboard filter dropdown.
    List<Application> findByStatus(ApplicationStatus status);

    // Check if a reference number already exists before saving a new application.
    // The service uses this to guarantee uniqueness when generating "APP-2025-001".
    boolean existsByReferenceNumber(String referenceNumber);

    // Find by reference number — useful if admin searches by ref number.
    Optional<Application> findByReferenceNumber(String referenceNumber);

    // Count how many applications exist per status —
    // used for the admin dashboard stats overview.
    long countByStatus(ApplicationStatus status);
}