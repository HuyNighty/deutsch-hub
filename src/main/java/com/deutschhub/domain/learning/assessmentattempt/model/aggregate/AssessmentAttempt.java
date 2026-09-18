package com.deutschhub.domain.learning.assessmentattempt.model.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.assessmentattempt.model.entity.AssessmentTaskAttempt;
import com.deutschhub.domain.learning.assessmentattempt.model.enums.AssessmentAttemptStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class AssessmentAttempt {

    private final UUID id;
    private final UUID assessmentId;
    private final UUID userId;

    private AssessmentAttemptStatus status;

    private Instant startedAt;
    private Instant expiresAt;

    private final List<AssessmentTaskAttempt> taskAttempts = new ArrayList<>();

    private AssessmentAttempt(UUID id, UUID assessmentId, UUID userId) {
        this.id = Objects.requireNonNull(id, "Assessment attempt id cannot be null");
        this.assessmentId = Objects.requireNonNull(assessmentId, "Assessment id cannot be null");
        this.userId = Objects.requireNonNull(userId, "User id cannot be null");
        this.status = AssessmentAttemptStatus.CREATED;
    }

    public static AssessmentAttempt create(UUID assessmentId, UUID userId) {
        return new AssessmentAttempt(UUID.randomUUID(), assessmentId, userId);
    }

    public void start(Integer timeLimitMinutes, Instant currentTime) {
        if (status != AssessmentAttemptStatus.CREATED) {
            throw new BusinessException(ErrorCode.ASSESSMENT_ATTEMPT_INVALID_STATUS);
        }

        Objects.requireNonNull(currentTime, "Current time cannot be null");

        if (timeLimitMinutes != null && timeLimitMinutes <= 0) {
            throw new BusinessException(ErrorCode.ASSESSMENT_ATTEMPT_INVALID_TIME_LIMIT);
        }

        this.startedAt = currentTime;

        if (timeLimitMinutes == null) {
            this.expiresAt = null;
        } else {
            expiresAt = currentTime.plus(timeLimitMinutes, ChronoUnit.MINUTES);
        }

        this.status = AssessmentAttemptStatus.IN_PROGRESS;
    }

    public void addTaskAttempt(AssessmentTaskAttempt taskAttempt) {
        ensureInProgress();
        Objects.requireNonNull(taskAttempt, "Task attempt cannot be null");

        boolean duplicateTask = taskAttempts.stream().anyMatch(existing ->
                existing.getTaskId().equals(taskAttempt.getTaskId())
        );

        if (duplicateTask) {
            throw new BusinessException(ErrorCode.ASSESSMENT_TASK_ATTEMPT_ALREADY_EXISTS);
        }

        taskAttempts.add(taskAttempt);
    }

    public void complete(Set<UUID> requiredTaskIds) {
        ensureInProgress();

        Objects.requireNonNull(requiredTaskIds, "Required task IDs cannot be null");

        if (requiredTaskIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ASSESSMENT_ATTEMPT_NO_REQUIRED_TASKS);
        }

        Set<UUID> attemptedTaskIds = taskAttempts.stream()
                .map(AssessmentTaskAttempt::getTaskId)
                .collect(Collectors.toSet());

        if (!attemptedTaskIds.equals(requiredTaskIds)) {
            throw new BusinessException(ErrorCode.ASSESSMENT_ATTEMPT_TASKS_NOT_COMPLETED);
        }

        this.status = AssessmentAttemptStatus.COMPLETED;
    }

    public void expire(Instant currentTime) {
        ensureInProgress();

        Objects.requireNonNull(currentTime, "Current time cannot be null");

        if (expiresAt == null || currentTime.isBefore(expiresAt)) {
            throw new BusinessException(ErrorCode.ASSESSMENT_ATTEMPT_NOT_EXPIRED);
        }

        this.status = AssessmentAttemptStatus.EXPIRED;
    }

    public void cancel() {
        ensureInProgress();
        this.status = AssessmentAttemptStatus.CANCELLED;
    }

    private void ensureInProgress() {
        if (status != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.ASSESSMENT_ATTEMPT_INVALID_STATUS);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public AssessmentAttemptStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public List<AssessmentTaskAttempt> getTaskAttempts() {
        return Collections.unmodifiableList(taskAttempts);
    }
}
