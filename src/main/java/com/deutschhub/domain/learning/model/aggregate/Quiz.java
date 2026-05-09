package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.entity.Question;
import com.deutschhub.domain.learning.model.valueobject.DifficultyLevel;
import com.deutschhub.domain.learning.model.valueobject.QuizStatus;
import com.deutschhub.domain.learning.model.valueobject.QuizVisibility;

import java.time.LocalDateTime;
import java.util.*;

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
        ensureCanMutateBy(createdBy, false);
        publishInternal();
    }

    public void publish(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        publishInternal();
    }

    private void publishInternal() {
        ensureNotDeleted();

        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_HAS_NO_QUESTIONS);
        }

        int totalScore = 0;

        for (Question question : questions) {
            question.validate();
            totalScore += question.getScore();
        }

        if (totalScore != this.maxScore) {
            throw new BusinessException(ErrorCode.QUIZ_SCORE_MISMATCH);
        }

        if (status == QuizStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_PUBLISHED);
        }

        if (status != QuizStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_STATE);
        }

        this.status = QuizStatus.PUBLISHED;
        this.touch();
    }

    public void unpublish() {
        ensureCanMutateBy(createdBy, false);
        unpublishInternal();
    }

    public void unpublish(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        unpublishInternal();
    }

    private void unpublishInternal() {
        if (status != QuizStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_STATE);
        }
        this.status = QuizStatus.DRAFT;
        touch();
    }

    public void addQuestion(Question question) {
        ensureCanMutateBy(createdBy, false);
        addQuestionInternal(question);
    }

    public void addQuestion(Question question, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        addQuestionInternal(question);
    }

    private void addQuestionInternal(Question question) {
        ensureNotDeleted();

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
        ensureCanMutateBy(createdBy, false);
        removeQuestionInternal(questionId);
    }

    public void removeQuestion(UUID questionId, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        removeQuestionInternal(questionId);
    }

    private void removeQuestionInternal(UUID questionId) {
        ensureNotDeleted();

        if (status == QuizStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_QUIZ);
        }

        boolean removed = questions.removeIf(q -> q.getId().equals(questionId));

        if (!removed) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        questions.removeIf(q -> q.getId().equals(questionId));
        this.touch();
    }

    public void updateTitle(String title) {
        ensureCanMutateBy(createdBy, false);
        updateTitleInternal(title);
    }

    public void updateTitle(String title, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        updateTitleInternal(title);
    }

    private void updateTitleInternal(String title) {
        this.title = validateTitle(title);
        this.touch();
    }

    public void updateDescription(String description) {
        ensureCanMutateBy(createdBy, false);
        updateDescriptionInternal(description);
    }

    public void updateDescription(String description, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        updateDescriptionInternal(description);
    }

    private void updateDescriptionInternal(String description) {
        this.description = description != null ? description.trim() : "";
        this.touch();
    }

    public void updateTimeLimit(int minutes) {
        this.timeLimitMinutes = validateTimeLimit(minutes);
        this.touch();
    }

    public void updateMaxScore(int score) {
        int newScore = validateMaxScore(score);

        int totalQuestionScore = questions.stream()
                .mapToInt(Question::getScore)
                .sum();

        if (totalQuestionScore > newScore) {
            throw new BusinessException(ErrorCode.QUIZ_SCORE_EXCEEDS_MAX);
        }

        this.maxScore = newScore;
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

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_DELETED);
        }
    }

    private void ensureCanMutateBy(UUID actorId, boolean isAdmin) {
        ensureNotDeleted();
        if (isAdmin) return;
        if (!createdBy.equals(actorId)) {
            throw new BusinessException(ErrorCode.QUIZ_FORBIDDEN_ACTION);
        }
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
        ensureCanMutateBy(createdBy, false);
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