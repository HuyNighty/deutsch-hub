package com.deutschhub.application.content.article.dto.response;

import java.util.List;

public record PublishedArticlePageResponse(
        List<PublishedArticleSummaryResponse> content,
        long totalElements,
        int page,
        int size
) {
}
