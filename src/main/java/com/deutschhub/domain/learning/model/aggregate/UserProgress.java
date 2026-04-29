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

    private static final double ALMOST_DONE_THRESHOLD = 80.0;

    private UserProgress(UUID id, UUID userId, UUID courseId, UUID enrollmentId, Progress currentProgress, UserProgressStatus status) {
        this.id = id;
        this.userId = validateNotNull(userId, "UserId");
        this.courseId = validateNotNull(courseId, "CourseId");
        this.enrollmentId = validateNotNull(enrollmentId, "EnrollmentId");
        this.currentProgress = currentProgress != null ? currentProgress : Progress.createInitial(0);
        this.status = status != null ? status : UserProgressStatus.NOT_STARTED;
        this.completedSections = 0;
        this.completedLessons = 0;
        this.totalStudyMinutes = 0;
        if (status == UserProgressStatus.NOT_STARTED) {
            this.startedAt = LocalDateTime.now();
        }        this.lastActivityAt = LocalDateTime.now();
        this.completedAt = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public static UserProgress create(UUID userId, UUID courseId, UUID enrollmentId) {
        return new UserProgress(UUID.randomUUID(), userId, courseId, enrollmentId, null, null);
    }

    public void recordLessonCompletion(int lessonMinutes) {
        ensureCanMutateBy(userId, false);
        recordLessonCompletionInternal(lessonMinutes);
    }

    public void recordLessonCompletion(int lessonMinutes, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        recordLessonCompletionInternal(lessonMinutes);
    }

    private void recordLessonCompletionInternal(int lessonMinutes) {
        ensureModifiable();
        this.completedLessons++;
        this.totalStudyMinutes += Math.max(0, lessonMinutes);
        this.lastActivityAt = LocalDateTime.now();

        this.currentProgress = this.currentProgress.updateProgress(this.completedLessons, this.totalStudyMinutes);

        if (this.currentProgress.isCompleted()) {
            this.status = UserProgressStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        } else if (this.currentProgress.getCompletionPercentage() >= ALMOST_DONE_THRESHOLD) {
            this.status = UserProgressStatus.ALMOST_DONE;
        } else {
            this.status = UserProgressStatus.IN_PROGRESS;
        }

        this.touch();
    }

    public void updateProgress(Progress newProgress) {
        ensureCanMutateBy(userId, false);
        updateProgressInternal(newProgress);
    }

    public void updateProgress(Progress newProgress, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        updateProgressInternal(newProgress);
    }

    private void updateProgressInternal(Progress newProgress) {
        ensureModifiable();

        if (newProgress == null) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA);
        }

        if (newProgress.getCompletionPercentage() < currentProgress.getCompletionPercentage()) {
            throw new BusinessException(ErrorCode.PROGRESS_CANNOT_DECREASE);
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
        ensureCanMutateBy(userId, false);
        markAsCompletedInternal();
    }

    public void markAsCompleted(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        markAsCompletedInternal();
    }

    private void markAsCompletedInternal() {
        ensureModifiable();
        if (this.status == UserProgressStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.USER_PROGRESS_ALREADY_COMPLETED);
        }

        this.status = UserProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.currentProgress = Progress.create(100, 100, this.totalStudyMinutes);
        this.touch();
    }

    public void recordSectionCompletion() {
        ensureCanMutateBy(userId, false);
        recordSectionCompletionInternal();
    }

    public void recordSectionCompletion(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        recordSectionCompletionInternal();
    }

    private void recordSectionCompletionInternal() {
        this.completedSections++;
        touch();
    }


    private void ensureModifiable() {
        ensureNotDeleted();
        ensureActive();
    }

    private <T> T validateNotNull(T value, String fieldName) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_PROGRESS_DATA,
                    fieldName + " cannot be null");
        }
        return value;
    }

    private void ensureActive() {
        if (status == UserProgressStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.USER_PROGRESS_ALREADY_COMPLETED);
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.USER_PROGRESS_ALREADY_DELETED);
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
        ensureCanMutateBy(userId, false);
        softDeleteInternal();
    }

    public void softDelete(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        softDeleteInternal();
    }

    private void softDeleteInternal() {
        this.deletedAt = LocalDateTime.now();
        this.touch();
    }

    private void ensureCanMutateBy(UUID actorId, boolean isAdmin) {
        ensureNotDeleted();
        if (isAdmin) return;
        if (!userId.equals(actorId)) {
            throw new BusinessException(ErrorCode.USER_PROGRESS_FORBIDDEN_ACTION);
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
