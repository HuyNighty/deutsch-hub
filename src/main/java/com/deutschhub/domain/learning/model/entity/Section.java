package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Section implements Auditable, SoftDeletable {

    private final UUID id;
    private String title;
    private String description;
    private int orderIndex;

    private final List<Lesson> lessons = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Section(UUID id, String title, String description, int orderIndex) {
        this.id = id;
        this.title = validateTile(title);
        this.description = description != null ? description : "";
        this.orderIndex = validateOrderIndex(orderIndex);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Section create(String title, String description, int orderIndex) {
        return new Section(UUID.randomUUID(), title, description, orderIndex);
    }

    public static Section restore( UUID id, String title, String description, int orderIndex,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        Section section = new Section(id, title, description, orderIndex);
        section.createdAt = createdAt;
        section.updatedAt = updatedAt;
        section.deletedAt = deletedAt;
        return section;
    }

    public void update(String title, String description, Integer orderIndex) {
        if (this.title != null) {
            changeTitle(title);
        }

        if (this.description != null) {
            this.description = description.trim();
        }

        if (orderIndex != null) {
            changeOrderIndex(orderIndex);
        }

        touch();
    }

    public void addLesson(Lesson lesson) {
        if (lesson == null) {
            throw new BusinessException(ErrorCode.LESSON_INVALID_CONTENT);
        }
        lessons.add(lesson);
        this.touch();
    }

    public void removeLesson(UUID lessonId) {
        lessons.removeIf(lesson -> lesson.getId().equals(lessonId));
        this.touch();
    }

    public void changeOrderIndex(int orderIndex) {
        if (orderIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_SECTION_ORDER);
        }
        this.orderIndex = orderIndex;
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

    private String validateTile(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SECTION_INVALID_TITLE);
        }
        return title.trim();
    }

    private int validateOrderIndex(int orderIndex) {
        if (orderIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_SECTION_ORDER);
        }
        return orderIndex;
    }

    private void changeTitle(String title) {
        if (title == null || title.trim().isBlank()) {
            throw new BusinessException(ErrorCode.SECTION_INVALID_TITLE);
        }

        this.title = title.trim();
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }
}
