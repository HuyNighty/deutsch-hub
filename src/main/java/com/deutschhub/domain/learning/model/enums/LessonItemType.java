package com.deutschhub.domain.learning.model.enums;

public enum LessonItemType {
    TEXT,
    MEDIA,
    QUIZ;

    public boolean requiresContent() {
        return this == TEXT;
    }

    public boolean requiresMedia() {
        return this == MEDIA;
    }

    public boolean requiresQuizId() {
        return this == QUIZ;
    }
}
