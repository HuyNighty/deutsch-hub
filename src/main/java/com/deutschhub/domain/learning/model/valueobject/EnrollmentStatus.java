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
        if (newStatus == null) {
            return false;
        }

        if (this == COMPLETED || this == DROPPED || this == EXPIRED) {
            return false;
        }

        if (this == ENROLLED) {
            return newStatus == IN_PROGRESS || newStatus == DROPPED || newStatus == EXPIRED;
        }

        if (this == IN_PROGRESS) {
            return newStatus == COMPLETED || newStatus == DROPPED || newStatus == EXPIRED;
        }

        return false;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isActive() {
        return this == ENROLLED || this == IN_PROGRESS;
    }
}
