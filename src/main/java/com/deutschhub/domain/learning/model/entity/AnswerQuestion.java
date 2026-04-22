package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Objects;
import java.util.UUID;

public class AnswerQuestion {

    private final UUID id;
    private String content;
    private boolean correct;

    private AnswerQuestion(UUID id, String content, boolean correct) {
        this.id = Objects.requireNonNull(id);
        this.content = validateContent(content);
        this.correct = correct;
    }

    public static AnswerQuestion create(String content, boolean correct) {
        return new AnswerQuestion(UUID.randomUUID(), content, correct);
    }

    private String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER);
        }
        return content.trim();
    }

    public void updateContent(String content) {
        this.content = validateContent(content);
    }

    public void markAsCorrect() {
        this.correct = true;
    }

    public void markAsIncorrect() {
        this.correct = false;
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public boolean isCorrect() {
        return correct;
    }
}
