package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.response.PublishedArticleDetailResponse;

public interface GetPublishedArticleBySlugUseCase {
    
    PublishedArticleDetailResponse getBySlug(String slug);
}
