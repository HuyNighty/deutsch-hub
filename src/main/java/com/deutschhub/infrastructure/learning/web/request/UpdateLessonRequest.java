package com.deutschhub.infrastructure.learning.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateLessonRequest(

        @Size(max = 150, message = "Lesson title must not exceed 150 characters")
        String title,

        @Size(max = 2000, message = "Lesson description must not exceed 150 characters")
        String description,

        String content,

        @Min(value = 1, message = "Estimated minutes must not be negative and greater than 0")
        Integer estimatedMinutes,

        String level,

        @Min(value = 0, message = "Order index must not be negative")
        Integer orderIndex,

        boolean freePreview
) {
}
