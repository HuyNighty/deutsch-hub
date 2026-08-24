package com.deutschhub.application.content.article.port.out;

import com.deutschhub.application.content.article.dto.query.ArticleDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.PublishedArticleDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.PageResult;
import com.deutschhub.application.content.article.dto.query.PublishedArticleQueryModel;
import com.deutschhub.domain.content.article.valueobject.Slug;

import java.util.Optional;
import java.util.UUID;

public interface ArticleQueryPort  {

    Optional<PublishedArticleDetailQueryModel> findPublishedBySlug(Slug slug);

    PageResult<PublishedArticleQueryModel> findPublishedArticles(int page, int size, String keyword, UUID categoryId, UUID topicId);

    Optional<ArticleDetailQueryModel> findDetailById(UUID articleId);

    boolean isMediaPubliclyReferenced(UUID mediaId);
}
