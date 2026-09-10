package com.deutschhub.domain.learning.quiz.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.enums.DifficultyLevel;
import com.deutschhub.domain.learning.quiz.model.enums.QuizRevisionStatus;

import java.util.*;

public class QuizRevision {

    private final UUID id;
    private final int revisionNumber;

    private QuizRevisionStatus status;

    private String title;
    private String description;
    private DifficultyLevel difficulty;

    private int timeLimitMinutes;
    private int passingPercentage;
    private int maxAttempts;

    private final List<Question> questions = new ArrayList<>();

    private QuizRevision(UUID id, int revisionNumber, String title, String description, DifficultyLevel difficulty,
                         int timeLimitMinutes, int passingPercentage, int maxAttempts) {
        this.id = Objects.requireNonNull(id);
        this.revisionNumber = revisionNumber;

        this.status = QuizRevisionStatus.DRAFT;

        this.title = validateTitle(title);
        this.description = description != null ? description.trim() : "";
        this.difficulty = Objects.requireNonNull(difficulty);

        this.timeLimitMinutes = validateTimeLimit(timeLimitMinutes);
        this.passingPercentage = validatePassingPercentage(passingPercentage);
        this.maxAttempts = validateMaxAttempts(maxAttempts);
    }

    public static QuizRevision createDraft(int revisionNumber, String title, String description, DifficultyLevel difficulty,
                                           int timeLimitMinutes, int passingPercentage, int maxAttempts) {
        return new QuizRevision(UUID.randomUUID(), revisionNumber, title, description, difficulty, timeLimitMinutes, passingPercentage, maxAttempts);
    }

    public int getMaxScore() {
        return questions.stream().mapToInt(Question::getScore).sum();
    }

    public void addQuestion(Question question) {
        ensureEditable();

        if (question == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        questions.add(question);
    }

    public void removeQuestion(UUID questionId) {
        ensureEditable();

        if (questionId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        boolean removed = questions.removeIf(q -> q.getId().equals(questionId));

        if (!removed) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }
    }

    public void submitForReview() {
        if (status != QuizRevisionStatus.DRAFT) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        validate();

        this.status = QuizRevisionStatus.IN_REVIEW;
    }

    public void withdrawSubmission() {
        if (status != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        this.status = QuizRevisionStatus.DRAFT;
    }

    public void publish() {
        if (status != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        validate();

        this.status = QuizRevisionStatus.PUBLISHED;
    }

    public void markHistorical() {
        if (status != QuizRevisionStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        this.status = QuizRevisionStatus.HISTORICAL;
    }

    public void updateTitle(String title) {
        ensureEditable();
        this.title = validateTitle(title);
    }

    public void updateDescription(String description) {
        ensureEditable();
        this.description = description != null ? description.trim() : "";
    }

    public void updateDifficulty(DifficultyLevel difficulty) {
        ensureEditable();
        this.difficulty = Objects.requireNonNull(difficulty);
    }

    public void updateTimeLimit(int minutes) {
        ensureEditable();
        this.timeLimitMinutes = validateTimeLimit(minutes);
    }

    public void updatePassingPercentage(int percentage) {
        ensureEditable();
        this.passingPercentage = validatePassingPercentage(percentage);
    }

    public void updateMaxAttempts(int maxAttempts) {
        ensureEditable();
        this.maxAttempts = validateMaxAttempts(maxAttempts);
    }

    private void ensureEditable() {
        if (status != QuizRevisionStatus.DRAFT) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }
    }

    private void validate() {
        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_HAS_NO_QUESTIONS);
        }

        questions.forEach(Question::validate);
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

    private int validatePassingPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_PASSING_SCORE);
        }
        return percentage;
    }

    private int validateMaxAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_MAX_ATTEMPTS);
        }
        return maxAttempts;
    }

    public UUID getId() {
        return id;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public QuizRevisionStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public int getPassingPercentage() {
        return passingPercentage;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

}
