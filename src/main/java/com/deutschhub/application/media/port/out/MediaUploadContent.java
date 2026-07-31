package com.deutschhub.application.media.port.out;

import com.deutschhub.domain.media.model.valueobject.MediaType;

import java.io.InputStream;

public record MediaUploadContent(
        String originalFileName,
        MediaType mediaType,
        String mimeType,
        long sizeBytes,
        InputStream inputStream
) {
}
