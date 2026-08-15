package com.deutschhub.application.content.topic.dto.response;

import com.deutschhub.domain.content.topic.enums.TopicStatus;

import java.util.UUID;

public record TopicResponse (
        UUID id,
        UUID categoryId,
        String name,
        TopicStatus status
) {
}
