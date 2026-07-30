package com.deutschhub.application.media.dto.response;

import com.deutschhub.domain.media.model.valueobject.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        String originalFileName,
        MediaType mediaType,
        String mimeType,
        long sizeBytes,
        UUID uploadedBy,
        LocalDateTime createdAt
) {
}
