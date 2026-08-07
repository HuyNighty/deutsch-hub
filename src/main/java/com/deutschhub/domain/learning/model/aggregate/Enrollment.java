package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.enums.EnrollmentStatus;
import com.deutschhub.domain.learning.model.valueobject.Progress;

import java.time.LocalDateTime;
import java.util.UUID;

public class Enrollment implements Auditable {

    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private EnrollmentStatus status;
    private Progress progress;
    private final LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime droppedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Enrollment(UUID id, UUID userId, UUID courseId, EnrollmentStatus status, Progress progress, LocalDateTime enrolledAt,
                       LocalDateTime completedAt, LocalDateTime droppedAt, LocalDateTime expiredAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ID_CAN_NOT_NULL);
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_CAN_NOT_NULL);
        }

        if (courseId == null) {
            throw new BusinessException(ErrorCode.COURSE_ID_CAN_NOT_NULL);
        }

        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_ENROLLMENT_STATUS);
        }

        if (progress == null) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA);
        }

        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
        this.progress = progress;
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
        this.droppedAt = droppedAt;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Enrollment create(UUID userId, UUID courseId, int totalLessons) {
        LocalDateTime now = LocalDateTime.now();

        return new Enrollment(UUID.randomUUID(), userId, courseId, EnrollmentStatus.ENROLLED, Progress.createInitial(totalLessons),
                now, null, null, null, now, now);
    }

    public static Enrollment restore(UUID id, UUID userId, UUID courseId, EnrollmentStatus status, Progress progress,
                                     LocalDateTime enrolledAt, LocalDateTime completedAt, LocalDateTime droppedAt,
                                     LocalDateTime expiredAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Enrollment(id, userId, courseId, status, progress, enrolledAt, completedAt, droppedAt, expiredAt,
                createdAt, updatedAt);
    }

    public boolean isActive() {
        return status.isActive();
    }

    public void updateProgress(int completedLessons, int totalStudyMinutes) {
        if (!status.isActive()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_ACTIVE);
        }

        progress = progress.updateProgress(completedLessons, totalStudyMinutes);

        if (status.canTransitionTo(EnrollmentStatus.IN_PROGRESS)
                && progress.hasStarted()) {

            status = EnrollmentStatus.IN_PROGRESS;
        }

        if (status.canTransitionTo(EnrollmentStatus.COMPLETED)
                && progress.isCompleted()) {

            status = EnrollmentStatus.COMPLETED;
            completedAt = LocalDateTime.now();
        }

        touch();
    }

    public void drop() {
        changeStatus(EnrollmentStatus.DROPPED, ErrorCode.ENROLLMENT_CAN_NOT_BE_DROPPED);
        this.droppedAt = LocalDateTime.now();
    }

    public void expire() {
        if (!status.canTransitionTo(EnrollmentStatus.EXPIRED)) {
            throw new BusinessException(ErrorCode.ENROLLMENT_CAN_NOT_BE_EXPIRED);
        }

        this.status = EnrollmentStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
        touch();
    }

    private void changeStatus(EnrollmentStatus newStatus, ErrorCode errorCode) {
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessException(errorCode);
        }

        this.status = newStatus;
        touch();
    }

    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getDroppedAt() {
        return droppedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
