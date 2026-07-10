package com.school.application.platform.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_news")
@Data
public class SchoolNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Full article body — TEXT allows unlimited length
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Short summary shown on the news card before clicking to read more
    @Column(columnDefinition = "TEXT")
    private String summary;

    // Category helps readers filter — e.g. "Announcement", "Event", "Update"
    @Column(nullable = false)
    private String category;

    // Who posted this — stored as the admin's email for display
    @Column(name = "posted_by", nullable = false)
    private String postedBy;

    // Whether this post is visible to the public.
    // Admin can save a draft (published=false) before making it live.
    @Column(nullable = false)
    private boolean published = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}