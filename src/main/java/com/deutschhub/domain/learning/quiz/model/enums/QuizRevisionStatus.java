package com.deutschhub.domain.learning.quiz.model.enums;

public enum QuizRevisionStatus {

    DRAFT("Draft"),
    IN_REVIEW("In Review"),
    PUBLISHED("Published"),
    HISTORICAL("Historical");

    private final String displayName;

    QuizRevisionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isInReview() {
        return this == IN_REVIEW;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean isHistorical() {
        return this == HISTORICAL;
    }

    public boolean isImmutable() {
        return this == PUBLISHED || this == HISTORICAL;
    }
}