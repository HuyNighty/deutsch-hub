package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.query.PublishedArticleDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.SourceQueryModel;
import com.deutschhub.application.content.article.dto.response.PublishedArticleDetailResponse;
import com.deutschhub.application.content.article.dto.response.SourceResponse;
import com.deutschhub.application.content.article.port.in.GetPublishedArticleBySlugUseCase;
import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.content.article.valueobject.Slug;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class GetPublishedArticleBySlugService implements GetPublishedArticleBySlugUseCase {

    ArticleQueryPort articleQueryPort;

    @Override
    public PublishedArticleDetailResponse getBySlug(String slug) {

        Slug articleSlug = new Slug(slug);

        PublishedArticleDetailQueryModel article = articleQueryPort.findPublishedBySlug(articleSlug)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        if (article.publicationStatus() == PublicationStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.ARTICLE_ARCHIVED);
        }

        if (article.publicationStatus() !=  PublicationStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        return toResponse(article);
    }

    private PublishedArticleDetailResponse toResponse(PublishedArticleDetailQueryModel article) {
        return new PublishedArticleDetailResponse( article.articleId(), article.versionId(), article.slug(), article.title(),
                article.summary(), article.body(), article.primaryCategoryId(), article.topicIds(), article.coverMediaId(),
                article.sources().stream().map(this::toSourceResponse).toList(), article.publishedAt());
    }

    private SourceResponse toSourceResponse(SourceQueryModel source) {
        return new SourceResponse(source.url(), source.title());
    }
}
