package com.school.application.platform.backend.repositories;

import com.school.application.platform.backend.entities.SchoolNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolNewsRepository extends JpaRepository<SchoolNews, Long> {

    // ── Public queries ────────────────────────────────────────────

    // Returns only published posts — used by the public /news page.
    // Unauthenticated users only ever see published content.
    List<SchoolNews> findByPublishedTrueOrderByCreatedAtDesc();

    // Filter published posts by category e.g. "Announcement"
    List<SchoolNews> findByPublishedTrueAndCategoryOrderByCreatedAtDesc(String category);

    // ── Admin queries ─────────────────────────────────────────────

    // Returns ALL posts (published + drafts) — used on the admin manage page.
    // Ordered newest first so the admin sees their latest posts at the top.
    List<SchoolNews> findAllByOrderByCreatedAtDesc();

    // Returns only drafts — useful for the admin to see what still needs publishing.
    List<SchoolNews> findByPublishedFalseOrderByCreatedAtDesc();

    // Count how many posts are published vs draft — used for admin stats.
    long countByPublishedTrue();
    long countByPublishedFalse();
}