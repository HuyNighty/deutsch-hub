package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.Progress;
import com.deutschhub.domain.learning.model.valueobject.UserProgressStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProgress implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private final UUID enrollmentId;
    private Progress currentProgress;
    private int completedSections;
    private int completedLessons;
    private int totalStudyMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime completedAt;
    private UserProgressStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private UserProgress(UUID id, UUID userId, UUID courseId, UUID enrollmentId, Progress currentProgress, UserProgressStatus status) {
        this.id = id;
        this.userId = validateNotNull(userId, "UserId");
        this.courseId = validateNotNull(courseId, "CourseId");
        this.enrollmentId = validateNotNull(enrollmentId, "EnrollmentId");
        this.currentProgress = currentProgress != null ? currentProgress : Progress.createInitial(0);
        this.status = UserProgressStatus.NOT_STARTED;
        this.completedSections = 0;
        this.completedLessons = 0;
        this.totalStudyMinutes = 0;
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.completedAt = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public static UserProgress create(UUID userId, UUID courseId, UUID enrollmentId) {
        return new UserProgress(UUID.randomUUID(), userId, courseId, enrollmentId, null, null);
    }

    public void recordLessonCompletion(int lessonMinutes) {
        this.completedLessons++;
        this.totalStudyMinutes += Math.max(0, lessonMinutes);
        this.lastActivityAt = LocalDateTime.now();

        this.currentProgress = this.currentProgress.updateProgress(this.completedLessons, this.totalStudyMinutes);

        if (this.currentProgress.isCompleted()) {
            this.status = UserProgressStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        } else if (this.currentProgress.getCompletionPercentage() >= 80.0) {
            this.status = UserProgressStatus.ALMOST_DONE;
        } else {
            this.status = UserProgressStatus.IN_PROGRESS;
        }

        this.touch();
    }

    public void updateProgress(Progress newProgress) {
        if (newProgress == null) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA);
        }

        this.currentProgress = newProgress;
        this.lastActivityAt = LocalDateTime.now();

        if (newProgress.isCompleted()) {
            this.status = UserProgressStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }

        this.touch();
    }

    public void markAsCompleted() {
        if (this.status == UserProgressStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.USER_PROGRESS_ALREADY_COMPLETED);
        }

        this.status = UserProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.currentProgress = Progress.create(100, 100, this.totalStudyMinutes); // Đảm bảo 100%
        this.touch();
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
    public UserProgressStatus getStatus() { return status; }
}
