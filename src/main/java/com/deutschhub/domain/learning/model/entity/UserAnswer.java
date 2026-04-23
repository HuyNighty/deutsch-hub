package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.*;

public class UserAnswer {

    private final UUID id;
    private final UUID questionId;
    private Set<UUID> selectedAnswerIds;

    private boolean correct;

    private UserAnswer(UUID id, UUID questionId, Set<UUID> selectedAnswerId) {
        this.id = Objects.requireNonNull(id);
        this.questionId = validateNotNull(questionId, "QuestionId");
        this.selectedAnswerIds = Collections.unmodifiableSet(new HashSet<>(selectedAnswerIds));
        if (this.selectedAnswerIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_USER_ANSWER_DATA,
                    "At least one answer must be selected");
        }    }

    public static UserAnswer single(UUID questionId, UUID selectedAnswerId) {
        return new UserAnswer(UUID.randomUUID(), questionId, Set.of(selectedAnswerId));
    }

    public static UserAnswer multiple(UUID questionId, Set<UUID> selectedAnswerIds) {
        return new UserAnswer(UUID.randomUUID(), questionId, selectedAnswerIds);
    }

    public UUID getSingleSelectedAnswerId() {
        if (selectedAnswerIds.size() != 1) {
            throw new BusinessException(ErrorCode.INVALID_USER_ANSWER_DATA,
                    "Expected single answer but got " + selectedAnswerIds.size());
        }
        return selectedAnswerIds.iterator().next();
    }

    private <T> T validateNotNull(T value, String field) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_ANSWER_DATA,
                    field + " cannot be null");
        }
        return value;
    }

    public void markCorrect() {
        this.correct = true;
    }

    public void markIncorrect() {
        this.correct = false;
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

    public boolean isCorrect() {
        return correct;
    }
}