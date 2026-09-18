package com.deutschhub.domain.learning.assessmentattempt.model.entity;

import java.util.Objects;
import java.util.UUID;

public class AssessmentTaskAttempt {

    private final UUID id;
    private final UUID taskId;
    private final UUID quizAttemptId;

    private AssessmentTaskAttempt(UUID id, UUID taskId, UUID quizAttemptId) {
        this.id = Objects.requireNonNull(id, "Assessment task attempt id cannot be null");
        this.taskId = Objects.requireNonNull(taskId, "Task id cannot be null");
        this.quizAttemptId = Objects.requireNonNull(quizAttemptId, "Quiz attempt id cannot be null");
    }

    public static AssessmentTaskAttempt create(UUID taskId, UUID quizAttemptId) {
        return new AssessmentTaskAttempt(UUID.randomUUID(), taskId, quizAttemptId);
    }

    public static AssessmentTaskAttempt restore(UUID id, UUID taskId, UUID quizAttemptId) {
        return new AssessmentTaskAttempt(id, taskId, quizAttemptId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getQuizAttemptId() {
        return quizAttemptId;
    }
}
