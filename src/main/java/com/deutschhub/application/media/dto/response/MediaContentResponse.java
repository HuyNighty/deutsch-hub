package com.deutschhub.application.media.dto.response;

import java.io.InputStream;

public record MediaContentResponse (
        InputStream inputStream,
        String mimeType,
        String originalFileName,
        long sizeBytes
) {
}
