package com.deutschhub.application.learning.dto.request;

import java.util.UUID;

public record UpdateSectionCommand(
        UUID courseId,
        UUID sectionId,
        UUID actorId,
        String title,
        String description,
        Integer orderIndex,
        boolean admin
) {
}
