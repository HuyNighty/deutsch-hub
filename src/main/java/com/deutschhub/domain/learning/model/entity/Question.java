package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.valueobject.QuestionType;

import java.util.*;

public class Question {

    private final UUID id;
    private String content;
    private int score;
    private QuestionType type;

    private final List<AnswerQuestion> answers = new ArrayList<>();

    private static final int MIN_ANSWERS = 2;
    private static final int MAX_ANSWERS = 6;

    private Question(UUID id, String content, int score, QuestionType type) {
        this.id = Objects.requireNonNull(id);
        this.content = validateContent(content);
        this.score = validateScore(score);
        this.type = Objects.requireNonNull(type);
    }

    public static Question create(String content, int score, QuestionType type) {
        return new Question(UUID.randomUUID(), content, score, type);
    }

    public void addAnswer(AnswerQuestion answer) {
        if (answer == null) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER);
        }

        if (answers.size() >= MAX_ANSWERS) {
            throw new BusinessException(ErrorCode.TOO_MANY_ANSWERS);
        }

        boolean duplicated = answers.stream()
                .anyMatch(a -> a.getContent().equalsIgnoreCase(answer.getContent()));

        if (duplicated) {
            throw new BusinessException(ErrorCode.DUPLICATE_ANSWER);
        }

        this.answers.add(answer);
    }

    public void removeAnswer(UUID answerId) {
        boolean removed = answers.removeIf(a -> a.getId().equals(answerId));

        if (!removed) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
    }

    public void validate() {
        validateAnswers(this.answers);
    }

    private void validateAnswers(List<AnswerQuestion> answers) {
        if (answers.size() < MIN_ANSWERS) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_ENOUGH_ANSWERS);
        }

        long correctCount = answers.stream()
                .filter(AnswerQuestion::isCorrect)
                .count();

        if (correctCount == 0) {
            throw new BusinessException(ErrorCode.QUESTION_NO_CORRECT_ANSWER);
        }

        if (type.isSingleChoice() && correctCount > 1) {
            throw new BusinessException(ErrorCode.QUESTION_TOO_MANY_CORRECT_ANSWERS);
        }

        if (type.isTrueFalse()) {
            if (answers.size() != 2 || correctCount != 1) {
                throw new BusinessException(ErrorCode.QUESTION_TRUE_FALSE_INVALID);
            }
        }
    }

    public void updateContent(String content) {
        this.content = validateContent(content);
    }

    public void updateScore(int score) {
        this.score = validateScore(score);
    }

    public void changeType(QuestionType type) {
        this.type = Objects.requireNonNull(type);
        validate();
    }

    private String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_CONTENT);
        }
        return content.trim();
    }

    private int validateScore(int score) {
        if (score <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SCORE);
        }
        return score;
    }

    public UUID getId() { return id; }

    public String getContent() { return content; }

    public int getScore() { return score; }

    public QuestionType getType() { return type; }

    public List<AnswerQuestion> getAnswers() {
        return Collections.unmodifiableList(answers);
    }
}