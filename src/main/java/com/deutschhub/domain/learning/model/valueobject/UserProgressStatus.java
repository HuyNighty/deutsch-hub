package com.deutschhub.domain.learning.model.valueobject;

public enum UserProgressStatus {

    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    ALMOST_DONE("Almost Done"),
    COMPLETED("Completed"),
    PAUSED("Paused"),
    ABANDONED("Abandoned");

    private final String displayName;

    UserProgressStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == IN_PROGRESS || this == ALMOST_DONE || this == PAUSED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean hasStarted() {
        return this != NOT_STARTED && this != ABANDONED;
    }

    public boolean canTransitionTo(UserProgressStatus newStatus) {
        return this != COMPLETED && this != ABANDONED;
    }
}