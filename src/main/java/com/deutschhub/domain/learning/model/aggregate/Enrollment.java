package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.UUID;

public class Enrollment implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private String status;
    private String progress;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Enrollment(UUID id, UUID userId, UUID courseId, String status) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status != null ? status : null;
        this.progress = progress;
        this.enrolledAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Enrollment create(UUID userId, UUID courseId) {
        if (userId == null || courseId == null) {
            throw new BusinessException(ErrorCode.INVALID_ENROLLMENT_DATA);
        }
        return new Enrollment(UUID.randomUUID(), userId, courseId, null);
    }

    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.touch();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public String getStatus() {
        return status;
    }

    public String getProgress() {
        return progress;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
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
