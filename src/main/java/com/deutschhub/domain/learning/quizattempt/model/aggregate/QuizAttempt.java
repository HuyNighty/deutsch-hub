package com.deutschhub.domain.learning.quizattempt.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.entity.AnswerQuestion;
import com.deutschhub.domain.learning.quiz.model.entity.Question;
import com.deutschhub.domain.learning.quizattempt.model.entity.QuestionResult;
import com.deutschhub.domain.learning.quizattempt.model.entity.UserAnswer;
import com.deutschhub.domain.learning.quizattempt.model.enums.AttemptStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class QuizAttempt implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID quizId;
    private final UUID userId;
    private final UUID revisionId;

    private AttemptStatus status;

    private final Map<UUID, UserAnswer> answers = new HashMap<>();
    private final Map<UUID, QuestionResult> results = new HashMap<>();

    private int totalScore;

    private final Instant startedAt;
    private Instant submittedAt;
    private Instant expiresAt;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private QuizAttempt(UUID id, UUID quizId, UUID revisionId, UUID userId) {
        this.id = Objects.requireNonNull(id);
        this.quizId = Objects.requireNonNull(quizId);
        this.userId = Objects.requireNonNull(userId);
        this.revisionId = Objects.requireNonNull(revisionId);

        this.status = AttemptStatus.IN_PROGRESS;

        this.totalScore = 0;

        this.startedAt = Instant.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public static QuizAttempt create(UUID quizId, UUID revisionId,UUID userId) {
        if (quizId == null || revisionId  == null || userId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_ATTEMPT_DATA);
        }
        return new QuizAttempt(UUID.randomUUID(), quizId, revisionId, userId);
    }

    public void answerQuestion(UserAnswer answer) {
        ensureCanMutateBy(userId, false);
        answerQuestionInternal(answer);
    }

    public void answerQuestion(UserAnswer answer, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        answerQuestionInternal(answer);
    }

    private void answerQuestionInternal(UserAnswer answer) {
        ensureNotDeleted();
        ensureInProgress();

        if (answer == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_ANSWER);
        }

        UUID questionId = answer.getQuestionId();

        answers.put(questionId, answer);
        touch();
    }

    public void submit(List<Question> questions) {
        ensureCanMutateBy(userId, false);
        submitInternal(questions);
    }

    public void submit(List<Question> questions, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        submitInternal(questions);
    }

    private void submitInternal(List<Question> questions) {
        ensureNotDeleted();
        ensureInProgress();

        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_HAS_NO_QUESTIONS);
        }

        results.clear();

        int score = 0;

        for (Question question : questions) {
            question.validate();

            UUID questionId = question.getId();
            UserAnswer userAnswer = answers.get(questionId);

            QuestionResult result;

            if (userAnswer == null) {
                result = QuestionResult.unanswered(questionId);
            } else {
                Set<UUID> correctAnswerIds = question.getAnswers().stream()
                        .filter(AnswerQuestion::isCorrect)
                        .map(AnswerQuestion::getId)
                        .collect(Collectors.toSet());

               result = QuestionResult.evaluate(questionId, question.getType(), correctAnswerIds,
                       userAnswer.getSelectedAnswerIds(), question.getScore());
            }

            results.put(questionId, result);
            score += result.getEarnedScore();
        }

        this.totalScore = score;
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = Instant.now();

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

    private void ensureCanMutateBy(UUID actorId, boolean isAdmin) {
        ensureNotDeleted();
        if (isAdmin) {
            return;
        }
        if (!userId.equals(actorId)) {
            throw new BusinessException(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN_ACTION);
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

    public UUID getRevisionId() {
        return revisionId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getSubmittedAt() {
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

    public Map<UUID, QuestionResult> getResults() {
        return Collections.unmodifiableMap(results);
    }
}
