package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.entity.AnswerQuestion;
import com.deutschhub.domain.learning.model.entity.Question;
import com.deutschhub.domain.learning.model.entity.UserAnswer;
import com.deutschhub.domain.learning.model.valueobject.AttemptStatus;
import com.deutschhub.domain.learning.model.valueobject.QuestionType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        for (Question q : questions) {
            if (!q.getQuizId().equals(this.quizId)) {
                throw new BusinessException(ErrorCode.QUESTION_NOT_BELONG_TO_QUIZ);
            }
        }

        if (answers.size() != questions.size()) {
            throw new BusinessException(ErrorCode.QUIZ_ATTEMPT_NOT_ALL_ANSWERED);
        }

        int score = 0;

        for (Question question : questions) {
            question.validate();
            UserAnswer userAnswer = answers.get(question.getId());

            QuestionType type = question.getType();

            if (type == QuestionType.MULTIPLE_CHOICE) {
                Set<UUID> selectedIds = userAnswer.getSelectedAnswerIds();

                Set<UUID> correctIds = question.getAnswers().stream()
                        .filter(AnswerQuestion::isCorrect)
                        .map(AnswerQuestion::getId)
                        .collect(Collectors.toSet());

                boolean allExist = selectedIds.stream()
                        .allMatch(id -> question.getAnswers().stream().anyMatch(a -> a.getId().equals(id)));

                if (!allExist) {
                    throw new BusinessException(ErrorCode.INVALID_USER_ANSWER);
                }

                boolean isCorrect = selectedIds.equals(correctIds);
                if (isCorrect) {
                    totalScore += question.getScore();
                }
            } else {
                UUID selectedId = userAnswer.getSingleSelectedAnswerId();

                boolean isValid = question.getAnswers().stream()
                        .anyMatch(a -> a.getId().equals(selectedId));
                if (!isValid) {
                    throw new BusinessException(ErrorCode.INVALID_USER_ANSWER);
                }

                boolean isCorrect = question.getAnswers().stream()
                        .anyMatch(a -> a.getId().equals(selectedId) && a.isCorrect());
                if (isCorrect) {
                    totalScore += question.getScore();
                }
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
