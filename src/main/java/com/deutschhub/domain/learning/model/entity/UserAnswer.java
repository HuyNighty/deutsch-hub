package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Objects;
import java.util.UUID;

public class UserAnswer {

    private final UUID id;
    private final UUID questionId;
    private final UUID selectedAnswerId;

    private boolean correct;

    private UserAnswer(UUID id, UUID questionId, UUID selectedAnswerId, boolean correct) {
        this.id = Objects.requireNonNull(id);
        this.questionId = validateNotNull(questionId, "QuestionId");
        this.selectedAnswerId = validateNotNull(selectedAnswerId, "SelectedAnswerId");
        this.correct = correct;
    }

    public static UserAnswer create(UUID questionId, UUID selectedAnswerId, boolean correct) {
        return new UserAnswer(UUID.randomUUID(), questionId, selectedAnswerId, correct);
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

    public UUID getSelectedAnswerId() {
        return selectedAnswerId;
    }

    public boolean isCorrect() {
        return correct;
    }
}