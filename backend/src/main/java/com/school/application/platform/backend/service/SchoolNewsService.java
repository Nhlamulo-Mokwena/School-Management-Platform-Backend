package com.school.application.platform.backend.service;

import com.school.application.platform.backend.dto.SchoolNewsRequest;
import com.school.application.platform.backend.dto.SchoolNewsResponse;
import com.school.application.platform.backend.entities.SchoolNews;
import com.school.application.platform.backend.repositories.SchoolNewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SchoolNewsService {

    private final SchoolNewsRepository newsRepository;

    public SchoolNewsService(SchoolNewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // ── PUBLIC ────────────────────────────────────────────────────

    // Returns only published posts — called by the public /api/news endpoint.
    // No authentication needed for this one.
    public List<SchoolNewsResponse> getPublishedNews() {
        return newsRepository.findByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Filter published posts by category
    public List<SchoolNewsResponse> getPublishedByCategory(String category) {
        return newsRepository
                .findByPublishedTrueAndCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Single published post by id — for the detail/read-more view
    public SchoolNewsResponse getPublishedById(Long id) {
        SchoolNews news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));

        if (!news.isPublished()) {
            throw new RuntimeException("Post not found: " + id);
        }
        return toResponse(news);
    }

    // ── ADMIN ─────────────────────────────────────────────────────

    // Returns all posts including drafts — admin only
    public List<SchoolNewsResponse> getAllNews() {
        return newsRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Create a new post — email comes from the JWT via Authentication
    @Transactional
    public SchoolNewsResponse create(SchoolNewsRequest request, String adminEmail) {
        SchoolNews news = new SchoolNews();
        news.setTitle(request.title());
        news.setContent(request.content());
        news.setCategory(request.category());
        news.setPublished(request.published());
        news.setPostedBy(adminEmail);

        // Auto-generate summary from first 150 chars of content if not provided
        news.setSummary(
                request.summary() != null && !request.summary().isBlank()
                        ? request.summary()
                        : request.content().length() > 150
                        ? request.content().substring(0, 150) + "..."
                        : request.content()
        );

        return toResponse(newsRepository.save(news));
    }

    // Update an existing post — admin can edit title, content, category,
    // or flip published to true to make a draft go live
    @Transactional
    public SchoolNewsResponse update(Long id, SchoolNewsRequest request) {
        SchoolNews news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));

        news.setTitle(request.title());
        news.setContent(request.content());
        news.setCategory(request.category());
        news.setPublished(request.published());

        news.setSummary(
                request.summary() != null && !request.summary().isBlank()
                        ? request.summary()
                        : request.content().length() > 150
                        ? request.content().substring(0, 150) + "..."
                        : request.content()
        );

        return toResponse(newsRepository.save(news));
    }

    // Delete a post permanently
    @Transactional
    public void delete(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new RuntimeException("Post not found: " + id);
        }
        newsRepository.deleteById(id);
    }

    // Toggle published status — quick way to publish a draft or unpublish a post
    @Transactional
    public SchoolNewsResponse togglePublished(Long id) {
        SchoolNews news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));

        news.setPublished(!news.isPublished());
        return toResponse(newsRepository.save(news));
    }

    // Admin stats — published vs draft count
    public NewsStats getStats() {
        return new NewsStats(
                newsRepository.count(),
                newsRepository.countByPublishedTrue(),
                newsRepository.countByPublishedFalse()
        );
    }

    public record NewsStats(long total, long published, long drafts) {}

    // ── Private helper ────────────────────────────────────────────

    private SchoolNewsResponse toResponse(SchoolNews news) {
        return new SchoolNewsResponse(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.getSummary(),
                news.getCategory(),
                news.getPostedBy(),
                news.isPublished(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}
