package com.deutschhub.domain.media.model.valueobject;

import java.util.Locale;
import java.util.Set;

public enum MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
    DOCUMENT;

    private static final Set<String> DOCUMENT_MIME_TYPE = Set.of(
            "/application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.oasis.opendocument.text"
    );

    public boolean supportsMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }

        String normalizedMimeType = mimeType.trim().toLowerCase(Locale.ROOT);

        return switch (this) {
            case IMAGE ->  normalizedMimeType.startsWith("image/");
            case VIDEO ->  normalizedMimeType.startsWith("video/");
            case AUDIO ->  normalizedMimeType.startsWith("audio/");
            case PDF ->  normalizedMimeType.startsWith("application/pdf");
            case DOCUMENT ->  DOCUMENT_MIME_TYPE.contains(normalizedMimeType);
        };
    }
}
