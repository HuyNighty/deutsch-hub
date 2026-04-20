package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.Progress;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProgress implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private final UUID enrollmentId;
    private final Progress currentProgress;
    private int completedSections;
    private int completedLessons;
    private int totalStudyMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime completedAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private UserProgress(UUID id, UUID userId, UUID courseId, UUID enrollmentId, Progress currentProgress, String status) {
        this.id = id;
        this.userId = validateNotNull(userId, "UserId");
        this.courseId = validateNotNull(courseId, "CourseId");
        this.enrollmentId = validateNotNull(enrollmentId, "EnrollmentId");
        this.currentProgress = currentProgress != null ? currentProgress : Progress.createInitial(0);
        this.status = "";

        this.completedSections = 0;
        this.completedLessons = 0;
        this.totalStudyMinutes = 0;
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.completedAt = null;
    }

    public static UserProgress create(UUID userId, UUID courseId, UUID enrollmentId) {
        return new UserProgress(UUID.randomUUID(), userId, courseId, enrollmentId, null, null);
    }

    private <T> T validateNotNull(T value, String fieldName) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_PROGRESS_DATA,
                    fieldName + " cannot be null");
        }
        return value;
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

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCourseId() { return courseId; }
    public UUID getEnrollmentId() { return enrollmentId; }

    public Progress getCurrentProgress() { return currentProgress; }
    public int getCompletedSections() { return completedSections; }
    public int getCompletedLessons() { return completedLessons; }
    public int getTotalStudyMinutes() { return totalStudyMinutes; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getStatus() { return status; }
}
