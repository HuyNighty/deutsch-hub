package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<LessonItem> items = new ArrayList<>();

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

    public static Lesson create(String title, String description, String content, int estimatedMinutes,
                                CEFRLevel level, int orderIndex) {
        return new Lesson(UUID.randomUUID(), title, description, content, estimatedMinutes, level, orderIndex, false);
    }

    public static Lesson restore(UUID id, String title, String description, String content, int estimatedMinutes,
                                 CEFRLevel level, int orderIndex, boolean freePreview, LocalDateTime createdAt,
                                 LocalDateTime updatedAt, LocalDateTime deletedAt) {
        Lesson lesson = new Lesson(id, title, description, content, estimatedMinutes, level, orderIndex, freePreview);
        lesson.createdAt = createdAt;
        lesson.updatedAt = updatedAt;
        lesson.deletedAt = deletedAt;
        return lesson;
    }

    public void update(String title, String description, String content, Integer estimatedMinutes,
                       CEFRLevel level, Integer orderIndex, Boolean freePreview) {
        if (title != null) {
            changeTitle(title);
        }

        if (description != null) {
            this.description = description.trim();
        }

        if (content != null) {
            changeContent(content);
        }

        if (estimatedMinutes != null) {
            changeEstimatedMinutes(estimatedMinutes);
        }

        if (level != null) {
            this.level = level;
        }

        if (orderIndex != null) {
            changeOrderIndex(orderIndex);
        }

        if (freePreview != null) {
            this.isFreePreview = freePreview;
        }

        touch();
    }

    private void changeTitle(String title) {
        if (title == null || title.trim().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_TITLE);
        }

        this.title = title.trim();
    }

    private void changeContent(String content) {
        if (content == null || content.trim().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_CONTENT);
        }

        this.content = content.trim();
    }

    private void changeEstimatedMinutes(int estimatedMinutes) {
        if (estimatedMinutes <= 0) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_ESTIMATED_MINUTES);
        }

        this.estimatedMinutes = estimatedMinutes;
    }

    private void changeOrderIndex(int orderIndex) {
        if (orderIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_LESSON_ORDER);
        }
    }

    public void addItem(LessonItem item) {
        ensureNotDeleted();

        if (item == null) {
            throw new BusinessException(ErrorCode.INVALID_LESSON);
        }

        this.items.add(item);
        touch();
    }

    public void restoreItem(LessonItem item) {
        if (item == null) {
            return;
        }
        this.items.add(item);
    }

    public void removeItem(UUID itemId) {
        ensureNotDeleted();

        LessonItem item = items.stream()
                .filter(lessonItem -> lessonItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        item.softDelete();
        touch();
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }
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

    public List<LessonItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
