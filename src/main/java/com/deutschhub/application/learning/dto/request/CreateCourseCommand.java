package com.deutschhub.application.learning.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCourseCommand(
        String title,
        String description,
        String level,
        BigDecimal price,
        String currency,
        UUID instructorId
) {
}
