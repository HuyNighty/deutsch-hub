package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.EnrollmentStatus;
import com.deutschhub.domain.learning.model.valueobject.Progress;

import java.time.LocalDateTime;
import java.util.UUID;

public class Enrollment implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private EnrollmentStatus status;
    private Progress progress;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Enrollment(UUID id, UUID userId, UUID courseId, EnrollmentStatus status) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status != null ? status : EnrollmentStatus.ENROLLED;
        this.progress = Progress.createInitial(0);
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

    public void startLearning() {
        ensureNotDeleted();

        if (status != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.INVALID_ENROLLMENT_STATE);
        }

        this.status = EnrollmentStatus.IN_PROGRESS;
        this.touch();
    }

    public void updateProgress(Progress newProgress) {
        ensureModifiable();
        if (newProgress == null) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA);
        }
        if (newProgress.getCompletionPercentage() < this.progress.getCompletionPercentage()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_PROGRESS_CANNOT_DECREASE);
        }

        this.progress = newProgress;

        if (newProgress.isCompleted()) {
            this.status = EnrollmentStatus.COMPLETED;
            this.completedAt = this.completedAt == null ? LocalDateTime.now() : this.completedAt;

            if (!newProgress.isCompleted()) {
                throw new BusinessException(ErrorCode.INVALID_ENROLLMENT_PROGRESS_STATE);
            }
        }

        this.touch();
    }

    private void ensureModifiable() {
        ensureNotDeleted();
        ensureActive();
    }

    private void ensureActive() {
        if (status == EnrollmentStatus.COMPLETED || status == EnrollmentStatus.DROPPED) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_ACTIVE);
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_DELETED);
        }
    }

    public void drop() {
        ensureNotDeleted();
        if (status == EnrollmentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CANNOT_DROP_COMPLETED_ENROLLMENT);
        }
        this.status = EnrollmentStatus.DROPPED;
        this.touch();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isCompleted() {
        return status.isCompleted();
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

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Progress getProgress() {
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
