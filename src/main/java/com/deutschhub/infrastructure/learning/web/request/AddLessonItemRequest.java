package com.deutschhub.infrastructure.learning.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddLessonItemRequest(

        @NotBlank(message = "Lesson item type is required")
        String type,

        @NotBlank(message = "Lesson item title is required")
        @Size(max = 150, message = "Lesson item title must not exceed 150 characters")
        String title,

        @Size(max = 1000, message = "Lesson item description must not exceed 1000 characters")
        String description,

        String content,

        String resourceUrl,

        UUID quizId,

        @Min(value = 1, message = "Estimated minutes must be greater than 0")
        int estimatedMinutes,

        @Min(value = 0, message = "Order index must not be negative")
        int orderIndex
) {
}
