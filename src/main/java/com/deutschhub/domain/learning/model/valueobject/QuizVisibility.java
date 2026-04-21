package com.deutschhub.domain.learning.model.valueobject;

public enum QuizVisibility {

    PRIVATE("Private"),
    COURSE_ONLY("Course Only"),
    PUBLIC("Public");

    private final String displayName;

    QuizVisibility(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isPrivate() {
        return this == PRIVATE;
    }
}