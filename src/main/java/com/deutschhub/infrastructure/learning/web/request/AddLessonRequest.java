package com.deutschhub.infrastructure.learning.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddLessonRequest (

        @NotBlank(message = "Lesson title is required")
        @Size(max = 150, message = "Lesson title must not exceed 150 characters")
        String title,

        @Size(max = 1000, message = "Lesson description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Lesson content is required")
        String content,

        @Min(value = 1, message = "Estimated minutes must be greater than 0")
        int estimatedMinutes,

        @NotBlank(message = "Lesson level is required")
        String level,

        @Min(value = 0, message = "Order index must not be negative")
        int orderIndex
) {
}
