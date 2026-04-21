package com.deutschhub.domain.learning.model.valueobject;

public enum DifficultyLevel {

    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    VERY_HARD("Very Hard");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEasy() {
        return this == EASY;
    }

    public boolean isHard() {
        return this == HARD || this == VERY_HARD;
    }
}