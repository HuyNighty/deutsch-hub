package com.deutschhub.application.learning.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCourseCommand(

        UUID courseId,
        UUID actorId,
        String title,
        String description,
        String level,
        BigDecimal price,
        String currency,
        boolean admin
) {
}
