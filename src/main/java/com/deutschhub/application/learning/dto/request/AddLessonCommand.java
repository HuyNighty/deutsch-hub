package com.deutschhub.application.learning.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddLessonCommand (
        UUID courseId,
        UUID sectionId,
        UUID actorId,
        String title,
        String description,
        String content,
        int estimatedMinutes,
        String level,
        int orderIndex,
        boolean admin
){
}
