package com.deutschhub.domain.learning.model.valueobject;

public enum QuestionType {
    SINGLE_CHOICE("Single Choice"),
    MULTIPLE_CHOICE("Multiple Choice"),
    TRUE_FALSE("True/False");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSingleChoice() {
        return this == SINGLE_CHOICE;
    }

    public boolean isTrueFalse() {
        return this == TRUE_FALSE;
    }

}
