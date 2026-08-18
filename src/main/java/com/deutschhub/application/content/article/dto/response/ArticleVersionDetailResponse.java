package com.deutschhub.application.content.article.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ArticleVersionDetailResponse(
        UUID versionId,
        int versionNumber,
        String title,
        String summary,
        String body,
        UUID primaryCategoryId,
        Set<UUID> topicIds,
        UUID coverMediaId,
        List<SourceResponse> sources,
        UUID createdBy,
        Instant createdAt,
        UUID lastModifiedBy,
        Instant lastModifiedAt
) {
}
