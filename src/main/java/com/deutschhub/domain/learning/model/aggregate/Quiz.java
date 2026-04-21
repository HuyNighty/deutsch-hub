package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.DifficultyLevel;
import com.deutschhub.domain.learning.model.valueobject.QuizStatus;
import com.deutschhub.domain.learning.model.valueobject.QuizVisibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private int passingScore;

    private DifficultyLevel difficulty;
    private QuizVisibility visibility;
    private QuizStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private final List<Question> questions = new ArrayList<>();

    private Quiz(UUID id, UUID courseId, UUID createdBy, String title, String description,
                 int timeLimitMinutes, int maxScore, int passingScore, DifficultyLevel difficulty,
                 QuizVisibility visibility, QuizStatus status) {
        this.id = Objects.requireNonNull(id);
        this.courseId = Objects.requireNonNull(courseId);
        this.createdBy = Objects.requireNonNull(createdBy);

        this.title = validateTitle(title);
        this.description = description != null ? description.trim() : "";

        this.timeLimitMinutes = validateTimeLimit(timeLimitMinutes);
        this.maxScore = validateMaxScore(maxScore);
        this.passingScore = validatePassingScore(passingScore, maxScore);

        this.difficulty = Objects.requireNonNull(difficulty);
        this.visibility = Objects.requireNonNull(visibility);
        this.status = Objects.requireNonNull(status);

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public static Quiz createDraft(UUID courseId, UUID createdBy) {
        return new Quiz(UUID.randomUUID(), courseId, createdBy, "Untitled Quiz",
                "", 15, 100, 50, DifficultyLevel.MEDIUM,
                QuizVisibility.PRIVATE, QuizStatus.DRAFT);
    }

    public void publish() {
        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_HAS_NO_QUESTIONS);
        }
        if (status == QuizStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_PUBLISHED);
        }
        this.status = QuizStatus.PUBLISHED;
        this.touch();
    }

    public void addQuestion(Question question) {
        if (status == QuizStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_QUIZ);
        }
        if (question == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }
        this.questions.add(question);
        this.touch();
    }

    public void removeQuestion(UUID questionId) {
        if (status == QuizStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_QUIZ);
        }
        questions.removeIf(q -> q.getId().equals(questionId));
        this.touch();
    }

    public void updateTitle(String title) {
        this.title = validateTitle(title);
        this.touch();
    }

    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : "";
        this.touch();
    }

    public void updateTimeLimit(int minutes) {
        this.timeLimitMinutes = validateTimeLimit(minutes);
        this.touch();
    }

    public void updateMaxScore(int score) {
        this.maxScore = validateMaxScore(score);
        touch();
    }

    public void changeDifficulty(DifficultyLevel difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty);
        this.touch();
    }

    public void changeVisibility(QuizVisibility visibility) {
        this.visibility = Objects.requireNonNull(visibility);
        touch();
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_TITLE);
        }
        return title.trim();
    }

    private int validateTimeLimit(int minutes) {
        if (minutes <= 0) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_TIME_LIMIT);
        }
        return minutes;
    }

    private int validateMaxScore(int score) {
        if (score <= 0) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_MAX_SCORE);
        }
        return score;
    }

    private int validatePassingScore(int passingScore, int maxScore) {
        if (passingScore < 0 || passingScore > maxScore) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_PASSING_SCORE);
        }
        return passingScore;
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

    public int getPassingScore() {
        return passingScore;
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

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }
}