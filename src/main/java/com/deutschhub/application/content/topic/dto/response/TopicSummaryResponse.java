package com.deutschhub.application.content.topic.dto.response;

import java.util.UUID;

public record TopicSummaryResponse(
        UUID id,
        String name,
        UUID categoryId
) {
}
