package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.entity.Question;
import com.deutschhub.domain.learning.model.entity.UserAnswer;
import com.deutschhub.domain.learning.model.valueobject.AttemptStatus;

import java.time.LocalDateTime;
import java.util.*;

public class QuizAttempt implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID quizId;
    private final UUID userId;

    private AttemptStatus status;

    private Map<UUID, UserAnswer> answers = new HashMap<>();

    private int totalScore;

    private final LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private QuizAttempt(UUID id, UUID quizId, UUID userId) {
        this.id = Objects.requireNonNull(id);
        this.quizId = Objects.requireNonNull(quizId);
        this.userId = Objects.requireNonNull(userId);

        this.status = AttemptStatus.IN_PROGRESS;

        this.answers = new HashMap<>();
        this.totalScore = 0;

        this.startedAt = LocalDateTime.now();
        this.createdAt = this.startedAt;
        this.updatedAt = this.startedAt;
        this.deletedAt = null;
    }

    public static QuizAttempt create(UUID quizId, UUID userId) {
        if (quizId == null || userId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_ATTEMPT_DATA);
        }
        return new QuizAttempt(UUID.randomUUID(), quizId, userId);
    }

    public void answerQuestion(UserAnswer answer) {
        ensureNotDeleted();
        ensureInProgress();

        if (answer == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_ANSWER);
        }

        UUID questionId = answer.getQuestionId();

        if (answers.containsKey(questionId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ANSWER);
        }

        answers.put(questionId, answer);
        touch();
    }

    public void submit(List<Question> questions) {
        ensureNotDeleted();
        ensureInProgress();

        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_HAS_NO_QUESTIONS);
        }

        if (answers.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_ATTEMPT_EMPTY);
        }

        int score = 0;

        for (Question question : questions) {
            UserAnswer userAnswer = answers.get(question.getId());

            if (userAnswer != null && userAnswer.isCorrect()) {
                score += question.getScore();
            }
        }

        this.totalScore = score;
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();

        touch();
    }

    private void ensureInProgress() {
        if (status != AttemptStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_ATTEMPT_STATE);
        }
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ATTEMPT_ALREADY_DELETED);
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
        this.deletedAt = LocalDateTime.now();
        this.touch();
    }

    public UUID getId() {
        return id;
    }

    public UUID getQuizId() {
        return quizId;
    }

    public UUID getUserId() {
        return userId;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Map<UUID, UserAnswer> getAnswers() {
        return Collections.unmodifiableMap(answers);
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
