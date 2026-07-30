package com.deutschhub.application.media.dto.request;

import com.deutschhub.domain.media.model.valueobject.MediaType;

import java.io.InputStream;
import java.util.UUID;

public record UploadMediaCommand(
        String originalFileName,
        MediaType mediaType,
        String mimeType,
        long sizeBytes,
        InputStream inputStream,
        UUID uploadedBy
) {
}
