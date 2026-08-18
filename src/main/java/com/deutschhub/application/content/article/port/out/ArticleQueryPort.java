package com.deutschhub.application.content.article.port.out;

import com.deutschhub.application.content.article.dto.query.ArticleDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.ArticleQueryModel;
import com.deutschhub.application.content.article.dto.query.PageResult;
import com.deutschhub.domain.content.article.valueobject.Slug;

import java.util.Optional;
import java.util.UUID;

public interface ArticleQueryPort  {

    Optional<ArticleQueryModel> findBySlug(Slug slug);

    PageResult<ArticleQueryModel> findPublishedArticles(int page, int size);

    Optional<ArticleDetailQueryModel> findDetailById(UUID articleId);

}
