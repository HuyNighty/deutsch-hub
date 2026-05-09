package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public class Lesson implements Auditable, SoftDeletable {

    private final UUID id;
    private String title;
    private String description;
    private String content;
    private int estimatedMinutes;
    private CEFRLevel level;
    private int orderIndex;
    private boolean isFreePreview = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Lesson(UUID id, String title, String description, String content, int estimatedMinutes, CEFRLevel level, int orderIndex, boolean isFreePreview) {
        this.id = id;
        this.title = validateTitle(title);
        this.description = description != null ? description : "";
        this.content = content != null ? content : "";
        this.estimatedMinutes = validateDuration(estimatedMinutes);
        this.level = validateLevel(level);
        this.orderIndex = orderIndex;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Lesson create(String title, String description, String content, int estimatedMinutes, CEFRLevel level, int orderIndex) {
        return new Lesson(UUID.randomUUID(), title, description, content, estimatedMinutes, level, orderIndex, false);
    }

    public void updateContent(String newContent) {
        this.content = newContent != null ? newContent.trim() : "";
        this.touch();
    }

    public void maskAsFreePreview() {
        this.isFreePreview = true;
        this.touch();
    }

    public void changeOrder(int newOrderIndex) {
        if (newOrderIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_ORDER);
        }
        this.orderIndex = newOrderIndex;
        this.touch();
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
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.LESSON_INVALID_TITLE);
        }
        return title.trim();
    }

    private int validateDuration(int minutes) {
        if (minutes <= 0) {
            throw new BusinessException(ErrorCode.LESSON_INVALID_DURATION);
        }
        return minutes;
    }

    private CEFRLevel validateLevel(CEFRLevel level) {
        if (level == null) {
            throw new BusinessException(ErrorCode.INVALID_CEFR_LEVEL);
        }
        return level;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @Override
    public LocalDateTime getDeletedAt() {
        return this.deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public CEFRLevel getLevel() {
        return level;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public boolean isFreePreview() {
        return isFreePreview;
    }
}
