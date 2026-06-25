package com.school.application.platform.backend.service;

import com.school.application.platform.backend.dto.ApplicationRequest;
import com.school.application.platform.backend.dto.ApplicationResponse;
import com.school.application.platform.backend.dto.StatusUpdateRequest;
import com.school.application.platform.backend.entities.Application;
import com.school.application.platform.backend.entities.Application.ApplicationStatus;
import com.school.application.platform.backend.entities.User;
import com.school.application.platform.backend.repositories.ApplicationRepository;
import com.school.application.platform.backend.repositories.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository        userRepository;

    // Absolute path on your Windows machine — avoids Tomcat temp directory issues.
    // The folder will be created automatically if it doesn't exist.
    // Run this in PowerShell first:
    // New-Item -ItemType Directory -Force -Path "C: SchoolApply - uploads -documents"
    private static final String UPLOAD_DIR = "C:/SchoolApply/uploads/documents/";

    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository        = userRepository;
    }

    // ── PARENT: Submit a new application ─────────────────────────
    @Transactional
    public ApplicationResponse submit(
            ApplicationRequest request,
            MultipartFile birthCertificate,
            MultipartFile parentIdDoc,
            MultipartFile reportCard,
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String birthCertPath  = saveFile(birthCertificate, "birth-cert");
        String parentIdPath   = saveFile(parentIdDoc,      "parent-id");
        String reportCardPath = saveFile(reportCard,       "report-card");

        Application app = new Application();
        app.setUser(user);

        app.setChildFirstName(request.childFirstName());
        app.setChildLastName(request.childLastName());
        app.setChildDob(request.childDob());
        app.setChildIdNumber(request.childIdNumber());
        app.setGradeApplying(request.gradeApplying());

        app.setParentFirstName(request.parentFirstName());
        app.setParentLastName(request.parentLastName());
        app.setParentEmail(request.parentEmail());
        app.setParentPhone(request.parentPhone());
        app.setRelationship(request.relationship());
        app.setAddress(request.address());

        app.setSchoolName(request.schoolName());

        app.setBirthCertificatePath(birthCertPath);
        app.setParentIdPath(parentIdPath);
        app.setReportCardPath(reportCardPath);

        app.setReferenceNumber(generateReferenceNumber());

        Application saved = applicationRepository.save(app);
        return toResponse(saved);
    }

    // ── PARENT: Get all my applications ──────────────────────────
    public List<ApplicationResponse> getMyApplications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return applicationRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── PARENT: Get a single application (must belong to them) ───
    public ApplicationResponse getMyApplication(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Application app = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Application not found"));

        return toResponse(app);
    }

    // ── PARENT: Withdraw an application ──────────────────────────
    @Transactional
    public void withdraw(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Application app = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Application not found"));

        if (app.getStatus() == ApplicationStatus.ACCEPTED ||
                app.getStatus() == ApplicationStatus.DECLINED) {
            throw new IllegalStateException("Cannot withdraw a decided application");
        }

        applicationRepository.delete(app);
    }

    // ── ADMIN: Get all applications ───────────────────────────────
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── ADMIN: Get applications filtered by status ────────────────
    public List<ApplicationResponse> getByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── ADMIN: Approve or decline an application ──────────────────
    @Transactional
    public ApplicationResponse updateStatus(Long id, StatusUpdateRequest request) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));

        if (request.status() != ApplicationStatus.ACCEPTED &&
                request.status() != ApplicationStatus.DECLINED) {
            throw new IllegalArgumentException("Status must be ACCEPTED or DECLINED");
        }

        app.setStatus(request.status());
        app.setAdminNotes(request.adminNotes());

        return toResponse(applicationRepository.save(app));
    }

    // ── ADMIN: Dashboard stats ────────────────────────────────────
    public DashboardStats getDashboardStats() {
        return new DashboardStats(
                applicationRepository.count(),
                applicationRepository.countByStatus(ApplicationStatus.PENDING),
                applicationRepository.countByStatus(ApplicationStatus.UNDER_REVIEW),
                applicationRepository.countByStatus(ApplicationStatus.ACCEPTED),
                applicationRepository.countByStatus(ApplicationStatus.DECLINED)
        );
    }

    public record DashboardStats(
            long total,
            long pending,
            long underReview,
            long accepted,
            long declined
    ) {}

    // ── Private helpers ───────────────────────────────────────────

    private String saveFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            // Creates the full directory path if it doesn't exist yet
            Files.createDirectories(uploadPath);

            String original  = file.getOriginalFilename();
            String extension = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf("."))
                    : "";
            String filename = prefix + "_" + UUID.randomUUID() + extension;
            Path filePath   = uploadPath.resolve(filename);

            // Files.copy reads directly from the multipart input stream —
            // avoids the Tomcat temp file issue that transferTo() has on Windows.
            Files.copy(file.getInputStream(), filePath);

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + prefix, e);
        }
    }

    private String generateReferenceNumber() {
        String year = String.valueOf(Year.now().getValue());
        String ref;
        do {
            int num = (int)(Math.random() * 900000) + 100000;
            ref = "APP-" + year + "-" + num;
        } while (applicationRepository.existsByReferenceNumber(ref));
        return ref;
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getReferenceNumber(),
                app.getChildFirstName(),
                app.getChildLastName(),
                app.getChildDob(),
                app.getChildIdNumber(),
                app.getGradeApplying(),
                app.getParentFirstName(),
                app.getParentLastName(),
                app.getParentEmail(),
                app.getParentPhone(),
                app.getRelationship(),
                app.getAddress(),
                app.getSchoolName(),
                app.getBirthCertificatePath(),
                app.getParentIdPath(),
                app.getReportCardPath(),
                app.getStatus(),
                app.getAdminNotes(),
                app.getSubmittedAt(),
                app.getUpdatedAt()
        );
    }
}