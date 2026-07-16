package com.deutschhub.domain.learning.model.valueobject;

public enum LessonItemType {
    TEXT,
    VIDEO,
    PDF,
    DOCUMENT,
    AUDIO,
    QUIZ;

    public boolean requiresContent() {
        return this == TEXT;
    }

    public boolean requiresResourceUrl() {
        return this == VIDEO || this == PDF || this == DOCUMENT || this == AUDIO;
    }

    public boolean requiresQuizId() {
        return this == QUIZ;
    }
}
