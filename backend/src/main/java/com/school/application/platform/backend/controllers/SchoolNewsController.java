package com.school.application.platform.backend.controllers;

import com.school.application.platform.backend.dto.SchoolNewsRequest;
import com.school.application.platform.backend.dto.SchoolNewsResponse;
import com.school.application.platform.backend.service.ApplicationService;
import com.school.application.platform.backend.service.SchoolNewsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
public class SchoolNewsController {

    private final SchoolNewsService newsService;

    public SchoolNewsController(SchoolNewsService newsService) {
        this.newsService = newsService;
    }

    // ── PUBLIC endpoints — no token required ──────────────────────
    // These are permitted in SecurityConfig under .requestMatchers("/api/news/public/**").permitAll()

    // GET /api/news/public
    // Returns all published posts — visible to anyone, no login needed
    @GetMapping("/public")
    public ResponseEntity<List<SchoolNewsResponse>> getPublishedNews() {
        return ResponseEntity.ok(newsService.getPublishedNews());
    }

    // GET /api/news/public/{id}
    // Returns a single published post by id — for the detail/read-more view
    @GetMapping("/public/{id}")
    public ResponseEntity<SchoolNewsResponse> getPublishedById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getPublishedById(id));
    }

    // GET /api/news/public/category/{category}
    // Filter published posts by category e.g. /api/news/public/category/Announcement
    @GetMapping("/public/category/{category}")
    public ResponseEntity<List<SchoolNewsResponse>> getByCategory(
            @PathVariable String category
    ) {
        return ResponseEntity.ok(newsService.getPublishedByCategory(category));
    }

    // ── ADMIN endpoints — ROLE_ADMIN required ─────────────────────

    // GET /api/news/admin/all
    // Returns every post including drafts — admin only
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SchoolNewsResponse>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    // GET /api/news/admin/stats
    // Returns total, published, draft counts for the admin news dashboard
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolNewsService.NewsStats> getStats() {
        return ResponseEntity.ok(newsService.getStats());
    }

    // POST /api/news/admin
    // Admin creates a new post.
    // Authentication is injected by Spring — auth.getName() gives the admin's email
    // which gets stored as postedBy so readers know who published it.
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolNewsResponse> create(
            @RequestBody @Valid SchoolNewsRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(newsService.create(request, auth.getName()));
    }

    // PUT /api/news/admin/{id}
    // Admin updates an existing post — full replace of all fields.
    // PUT because the admin is replacing the whole post content.
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolNewsResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid SchoolNewsRequest request
    ) {
        return ResponseEntity.ok(newsService.update(id, request));
    }

    // DELETE /api/news/admin/{id}
    // Admin permanently deletes a post
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/news/admin/{id}/toggle
    // Quick publish/unpublish without editing the full post.
    // Useful for the admin to flip a draft live or take a post offline.
    @PatchMapping("/admin/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolNewsResponse> togglePublished(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.togglePublished(id));
    }

    // ── Exception handler ─────────────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }
}