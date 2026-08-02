package com.deutschhub.domain.media.model.valueobject;

import java.util.Locale;
import java.util.Set;

public enum MediaType {
    IMAGE(Set.of("image/png", "image/jpeg", "image/webp")),
    VIDEO(Set.of("video/mp4")),
    AUDIO(Set.of("audio/mpeg")),
    PDF(Set.of("application/pdf")),
    DOCUMENT(Set.of(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.oasis.opendocument.text",
            "text/plain"
    ));

    private final Set<String> supportedMimeTypes;

    MediaType(Set<String> supportedMimeTypes) {
        this.supportedMimeTypes = supportedMimeTypes;
    }

    public boolean supportsMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }

        return supportedMimeTypes.contains(mimeType.trim().toLowerCase(Locale.ROOT));
    }
}
