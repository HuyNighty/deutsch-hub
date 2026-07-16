package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.LessonItemType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class LessonItem implements Auditable, SoftDeletable {

    private final UUID id;
    private final LessonItemType type;
    private String title;
    private String description;
    private String content;
    private String resourceUrl;
    private UUID quizId;
    private int estimatedMinutes;
    private int orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private LessonItem(UUID id, LessonItemType type, String title, String description, String content,
                       String resourceUrl, UUID quizId, int estimatedMinutes, int orderIndex) {
        this.id = Objects.requireNonNull(id);
        this.type = validateType(type);
        this.title = validateTitle(title);
        this.description = description != null ? description.trim() :  "";
        this.content = content != null ? content.trim() : "";
        this.resourceUrl = resourceUrl != null ? resourceUrl.trim() : "";
        this.quizId = quizId;
        this.estimatedMinutes = validateEstimatedMinutes(estimatedMinutes);
        this.orderIndex = validateOrderIndex(orderIndex);
        validatePayload();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static LessonItem createText(String title, String description, String content,
                                        int estimatedMinutes, int orderIndex) {
        return new LessonItem(UUID.randomUUID(), LessonItemType.TEXT, title, description,
                content, null, null, estimatedMinutes, orderIndex);
    }

    public static LessonItem createResource(LessonItemType type, String title, String description,
                                            String resourceUrl,  int estimatedMinutes, int orderIndex) {
        return new LessonItem(UUID.randomUUID(), type, title, description, null, resourceUrl,
                null, estimatedMinutes, orderIndex);
    }

    public static LessonItem createQuiz(String title, String description, UUID quizId,
                                        int estimatedMinutes, int orderIndex) {
        return new LessonItem(UUID.randomUUID(), LessonItemType.QUIZ, title, description, null,
                null, quizId, estimatedMinutes, orderIndex);
    }

    public static LessonItem restore(UUID id, LessonItemType type, String title, String description,
                                     String content, String resourceUrl, UUID quizId,
                                     int estimatedMinutes, int orderIndex,
                                     LocalDateTime createdAt, LocalDateTime updatedAt,
                                     LocalDateTime deletedAt) {
        LessonItem item = new LessonItem(id, type, title, description, content, resourceUrl, quizId,
                estimatedMinutes, orderIndex);

        item.createdAt = createdAt;
        item.updatedAt = updatedAt;
        item.deletedAt = deletedAt;
        return item;
    }

    public void update(String title, String description, String content, String resourceUrl, UUID quizId,
                       Integer estimatedMinutes, Integer orderIndex) {
        ensureNotDeleted();

        if (title != null) {
            this.title = validateTitle(title);
        }

        if (description != null) {
            this.description = description.trim();
        }

        if (content != null) {
            this.content = content.trim();
        }

        if (resourceUrl != null) {
            this.resourceUrl = resourceUrl.trim();
        }

        if (quizId != null) {
            this.quizId = quizId;
        }

        if (estimatedMinutes != null) {
            this.estimatedMinutes = validateEstimatedMinutes(estimatedMinutes);
        }

        if (orderIndex != null) {
            this.orderIndex = validateOrderIndex(orderIndex);
        }

        validatePayload();
        touch();
    }

    private void validatePayload() {
        if (type.requiresContent() && content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_CONTENT);
        }

        if (type.requiresResourceUrl() && resourceUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_CONTENT);
        }

        if (type.requiresQuizId() && quizId == null) {
            throw new BusinessException(ErrorCode.INVALID_LESSON);
        }

        if (!type.requiresResourceUrl()) {
            this.resourceUrl = "";
        }

        if (!type.requiresQuizId()) {
            this.quizId = null;
        }
    }

    private LessonItemType validateType(LessonItemType type) {
        if (type == null) {
            throw new BusinessException(ErrorCode.INVALID_LESSON);
        }
        return type;
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_TITLE);
        }
        return title.trim();
    }

    private int validateEstimatedMinutes(int estimatedMinutes) {
        if (estimatedMinutes <= 0) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_ESTIMATED_MINUTES);
        }
        return estimatedMinutes;
    }

    private int validateOrderIndex(int orderIndex) {
        if (orderIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_ORDER);
        }
        return orderIndex;
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }
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
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public void softDelete() {
        ensureNotDeleted();
        this.deletedAt = LocalDateTime.now();
        touch();
    }

    public UUID getId() {
        return id;
    }

    public LessonItemType getType() {
        return type;
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

    public String getResourceUrl() {
        return resourceUrl;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
}
