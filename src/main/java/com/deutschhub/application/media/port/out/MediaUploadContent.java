package com.deutschhub.application.media.port.out;

import java.io.InputStream;

    public record MediaUploadContent(
        String originalFileName,
        String mimeType,
        long sizeBytes,
        InputStream inputStream
) {
}
