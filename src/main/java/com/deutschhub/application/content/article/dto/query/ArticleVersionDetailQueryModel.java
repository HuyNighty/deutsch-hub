package com.deutschhub.application.content.article.dto.query;

import com.deutschhub.application.content.article.dto.response.SourceResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ArticleVersionDetailQueryModel(
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
