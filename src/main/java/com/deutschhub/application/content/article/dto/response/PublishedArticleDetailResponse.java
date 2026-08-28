package com.deutschhub.application.content.article.dto.response;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;

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
        CategorySummaryResponse primaryCategory,
        Set<TopicSummaryResponse> topics,
        UUID coverMediaId,
        List<SourceResponse> sources,
        Instant publishedAt
) {
}
