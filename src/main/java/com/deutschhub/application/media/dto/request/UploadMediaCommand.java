package com.deutschhub.application.media.dto.request;

import java.io.InputStream;
import java.util.UUID;

public record UploadMediaCommand(
        String originalFileName,
        String mimeType,
        long sizeBytes,
        InputStream inputStream,
        UUID uploadedBy
) {
}
