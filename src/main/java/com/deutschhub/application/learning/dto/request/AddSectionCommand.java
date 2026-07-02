package com.deutschhub.application.learning.dto.request;

import java.util.UUID;

public record AddSectionCommand(

        UUID courseId,
        UUID actorId,
        String title,
        String description,
        int orderIndex,
        boolean admin
) {
}
