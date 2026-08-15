package com.deutschhub.application.content.topic.dto.request;

import java.util.UUID;

public record ReactivateTopicCommand(
        UUID topicId
) {
}
