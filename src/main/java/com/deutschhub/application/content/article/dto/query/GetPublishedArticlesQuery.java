package com.deutschhub.application.content.article.dto.query;

import java.util.UUID;

public record GetPublishedArticlesQuery(
        int page,
        int size,
        String keyword,
        UUID categoryId,
        UUID topicId
) {
}
