package com.deutschhub.infrastructure.content.topic.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTopicRequest(

        @NotBlank
        @Size(max = 100)
        String topicName
) {
}
