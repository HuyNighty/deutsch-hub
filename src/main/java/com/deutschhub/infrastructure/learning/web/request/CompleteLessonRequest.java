package com.deutschhub.infrastructure.learning.web.request;

import jakarta.validation.constraints.Min;

public record CompleteLessonRequest(

        @Min(value = 0, message = "Study minutes must not be negative")
        int studyMinutes
) {
}
