package com.deutschhub.domain.media.service;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.domain.media.model.valueobject.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MediaTypeResolverTest {

    private final MediaTypeResolver resolver = new MediaTypeResolver();

    @Test
    void shouldResolveImageMimeType() {
        assertEquals(MediaType.IMAGE, resolver.resolve("image/png"));
    }

    @Test
    void shouldNormalizeMimeType() {
        assertEquals(MediaType.IMAGE, resolver.resolve(" IMAGE/PNG "));
    }

    @Test
    void shouldRejectNullMimeType() {
        assertThrows(BusinessException.class, () -> resolver.resolve(null));
    }

    @Test
    void shouldRejectEmptyMimeType() {
        assertThrows(BusinessException.class, () -> resolver.resolve(""));
    }

    @Test
    void shouldRejectUnsupportedMimeType() {
        assertThrows(BusinessException.class, () -> resolver.resolve("application/xyz"));
    }
}
