package com.deutschhub.application.content.article.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PublishedArticleDetailResponse(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        String body,
        UUID primaryCategoryId,
        Set<UUID> topicIds,
        UUID coverMediaId,
        List<SourceResponse> sources,
        Instant publishedAt
) {
}
