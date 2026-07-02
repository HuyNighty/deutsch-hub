package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SectionResponse(
        UUID id,
        String title,
        String description,
        int orderIndex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
