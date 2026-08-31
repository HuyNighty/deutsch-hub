package com.deutschhub.application.content.article.dto.response;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record PublishedArticleSummaryResponse(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        CategorySummaryResponse primaryCategory,
        Set<TopicSummaryResponse> topics,
        UUID coverMediaId,
        Instant publishedAt
) {
}
