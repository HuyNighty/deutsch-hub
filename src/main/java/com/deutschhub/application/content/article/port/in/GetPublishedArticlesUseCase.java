package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.query.GetPublishedArticlesQuery;
import com.deutschhub.application.content.article.dto.response.PublishedArticlePageResponse;

public interface GetPublishedArticlesUseCase {

    PublishedArticlePageResponse getPublishedArticles(GetPublishedArticlesQuery query);
}
