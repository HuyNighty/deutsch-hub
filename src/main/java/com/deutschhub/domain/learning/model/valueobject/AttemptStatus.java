package com.deutschhub.domain.learning.model.valueobject;

public enum AttemptStatus {

    IN_PROGRESS("In Progress"),
    SUBMITTED("Submitted"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");

    private final String description;

    AttemptStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isSubmitted() {
        return this == SUBMITTED;
    }

    public boolean isFinalState() {
        return this == SUBMITTED || this == EXPIRED || this == CANCELLED;
    }
}
