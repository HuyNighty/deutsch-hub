package com.deutschhub.domain.content.model.valueObject;

public enum ArticleStatus {
    DRAFT("Draft"),
    SCHEDULED("Scheduled"),
    PUBLISHED("Published"),
    ARCHIVED("Archived");

    private final String displayName;

    ArticleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean canBeModified() {
        return this == DRAFT || this == SCHEDULED;
    }

    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }
}
