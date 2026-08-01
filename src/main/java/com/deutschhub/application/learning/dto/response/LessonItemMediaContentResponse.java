package com.deutschhub.application.learning.dto.response;

import java.io.InputStream;

public record LessonItemMediaContentResponse(
        InputStream inputStream,
        String mimeType,
        long sizeBytes,
        String originalFileName
) {
}
