package com.deutschhub.domain.learning.model.valueobject;

public enum EnrollmentStatus {
    ENROLLED("Enrolled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    DROPPED("Dropped"),
    EXPIRED("Expired");

    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(EnrollmentStatus newStatus) {
        return this != COMPLETED && this != IN_PROGRESS && this != ENROLLED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isActive() {
        return this == ENROLLED || this == IN_PROGRESS;
    }
}
