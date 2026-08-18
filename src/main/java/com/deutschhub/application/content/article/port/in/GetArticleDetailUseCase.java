package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.response.ArticleDetailResponse;

import java.util.UUID;

public interface GetArticleDetailUseCase {

    ArticleDetailResponse getById(UUID articleId);
}
