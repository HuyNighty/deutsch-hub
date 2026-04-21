package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Quiz implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID courseId;
    private final UUID createdBy;

    private String title;
    private String description;

    private int timeLimitMinutes;
    private int maxScore;

    private DifficultyLevel difficulty;
    private QuizVisibility visibility;
    private QuizStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Quiz(UUID id, UUID courseId, UUID createdBy, String title, String description, int timeLimitMinutes, int maxScore, DifficultyLevel difficulty, QuizVisibility visibility, QuizStatus status) {
        this.id = Objects.requireNonNull(id);
        this.courseId = Objects.requireNonNull(courseId);
        this.createdBy = Objects.requireNonNull(createdBy);

        this.title = validateTitle(title);
        this.description = description != null ? description.trim() : "";

        this.timeLimitMinutes = validateTimeLimit(timeLimitMinutes);
        this.maxScore = validateMaxScore(maxScore);

        this.difficulty = Objects.requireNonNull(difficulty);
        this.visibility = Objects.requireNonNull(visibility);
        this.status = Objects.requireNonNull(status);

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public static Quiz createDraft(UUID courseId, UUID createdBy) {
        return new Quiz(UUID.randomUUID(), courseId, createdBy, "Untitled Quiz", "", 15, 100, DifficultyLevel.MEDIUM, QuizVisibility.PRIVATE, QuizStatus.DRAFT);
    }

    public void updateTitle(String title) {
        this.title = validateTitle(title);
        touch();
    }

    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : "";
        touch();
    }

    public void updateTimeLimit(int minutes) {
        this.timeLimitMinutes = validateTimeLimit(minutes);
        touch();
    }

    public void updateMaxScore(int score) {
        this.maxScore = validateMaxScore(score);
        touch();
    }

    public void changeDifficulty(DifficultyLevel difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty);
        touch();
    }

    public void changeVisibility(QuizVisibility visibility) {
        this.visibility = Objects.requireNonNull(visibility);
        touch();
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

    public UUID getId() {
        return id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public QuizVisibility getVisibility() {
        return visibility;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}