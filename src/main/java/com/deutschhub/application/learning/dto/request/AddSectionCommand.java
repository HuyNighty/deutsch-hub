package com.deutschhub.application.learning.dto.request;

public record AddSectionCommand(
        String title,
        String description,
        int orderIndex
) {
}
