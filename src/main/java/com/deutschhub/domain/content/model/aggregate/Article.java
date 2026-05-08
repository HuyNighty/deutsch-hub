package com.deutschhub.domain.content.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.model.valueObject.ArticleStatus;
import com.deutschhub.domain.content.model.valueObject.Slug;

import java.time.LocalDateTime;
import java.util.*;

public class Article implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID authorId;
    private final UUID categoryId;
    private Slug slug;
    private String title;
    private String metaDescription;
    private ArticleStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime scheduledPublishedAt;
    private UUID featuredImageId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Article(UUID id, UUID authorId, UUID categoryId, String title, Slug slug) {
        this.id = Objects.requireNonNull(id);
        this.authorId = Objects.requireNonNull(authorId);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.title = validateTitle(title);
        this.slug = Objects.requireNonNull(slug);
        this.status = ArticleStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public static Article createDraft(UUID authorId, UUID categoryId, String title, Slug slug) {
        return new Article(UUID.randomUUID(), authorId, categoryId, title, slug);
    }


    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.touch();
    }

    private String validateTitle(String title) {
        return title == null || title.trim().isEmpty() ? null : title;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public Slug getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public ArticleStatus getStatus() {
        return status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getScheduledPublishedAt() {
        return scheduledPublishedAt;
    }

    public UUID getFeaturedImageId() {
        return featuredImageId;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
