package com.deutschhub.domain.learning.quizattempt.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.enums.QuestionType;
import com.deutschhub.domain.learning.quizattempt.model.enums.ResponseStatus;

import java.util.*;

public class QuestionResult {

    private final UUID id;
    private final UUID questionId;
    private final Set<UUID> selectedAnswerIds;
    private final ResponseStatus responseStatus;
    private final Boolean correct;
    private final int earnedScore;

    private QuestionResult(UUID id, UUID questionId, Set<UUID> selectedAnswerIds, ResponseStatus responseStatus, Boolean correct,
                           int earnedScore) {

        this.id = Objects.requireNonNull(id);
        this.questionId = Objects.requireNonNull(questionId);
        this.selectedAnswerIds = Collections.unmodifiableSet(Objects.requireNonNull(selectedAnswerIds));
        this.responseStatus = Objects.requireNonNull(responseStatus);
        this.correct = correct;
        this.earnedScore = earnedScore;
    }

    public static QuestionResult evaluate(UUID questionId, QuestionType questionType, Set<UUID> correctAnswerIds,
                                          Set<UUID> selectedAnswerIds, int questionScore) {
        validateEvaluationData(questionId, questionType, correctAnswerIds, selectedAnswerIds, questionScore);

        Set<UUID> selectedIds = Set.copyOf(Objects.requireNonNull(selectedAnswerIds));

        boolean isCorrect = switch (questionType) {
            case SINGLE_CHOICE, TRUE_FALSE -> selectedIds.size() == 1
                    && correctAnswerIds.size() == 1
                    && selectedIds.equals(correctAnswerIds);
            case MULTIPLE_CHOICE -> selectedIds.equals(correctAnswerIds);
            default -> throw new BusinessException(ErrorCode.INVALID_QUESTION_TYPE);
        };

        int earnedScore = isCorrect ? questionScore : 0;

        return new QuestionResult(UUID.randomUUID(), questionId, selectedIds, ResponseStatus.ANSWERED, isCorrect, earnedScore);
    }

    public static QuestionResult unanswered(UUID questionId) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_RESULT_QUESTION_ID);
        }

        return new QuestionResult(UUID.randomUUID(), questionId, Collections.emptySet(), ResponseStatus.UNANSWERED, null, 0);
    }

    private static void validateEvaluationData(UUID questionId, QuestionType questionType, Set<UUID> correctAnswerIds,
                                               Set<UUID> selectedAnswerIds, int questionScore) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_RESULT_QUESTION_ID);
        }

        if (questionType == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_RESULT_QUESTION_TYPE);
        }

        if (correctAnswerIds == null || correctAnswerIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_RESULT_CORRECT_ANSWERS);
        }

        if (selectedAnswerIds == null || selectedAnswerIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_RESULT_SELECTED_ANSWERS);
        }

        if (questionScore < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_RESULT_SCORE);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public Set<UUID> getSelectedAnswerIds() {
        return selectedAnswerIds;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public Boolean isCorrect() {
        return correct;
    }

    public int getEarnedScore() {
        return earnedScore;
    }
}
