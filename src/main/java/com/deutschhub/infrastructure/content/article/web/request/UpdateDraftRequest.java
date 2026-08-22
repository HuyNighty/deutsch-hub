package com.deutschhub.infrastructure.content.article.web.request;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UpdateDraftRequest(
        @Size(max = 255)
        String title,

        String summary,

        String body,

        UUID primaryCategoryId,

        Set<UUID> topicIds,

        UUID coverMediaId,

        List<SourceRequest> sources
) {
}
