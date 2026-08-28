package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.query.CategorySummaryQuery;
import com.deutschhub.application.content.article.dto.query.PublishedArticleDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.SourceQueryModel;
import com.deutschhub.application.content.article.dto.query.TopicSummaryQuery;
import com.deutschhub.application.content.article.dto.response.PublishedArticleDetailResponse;
import com.deutschhub.application.content.article.dto.response.SourceResponse;
import com.deutschhub.application.content.article.port.in.GetPublishedArticleBySlugUseCase;
import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.content.article.valueobject.Slug;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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
                article.summary(), article.body(), toCategoryResponse(article.primaryCategory()),
                toTopicResponses(article.topics()), article.coverMediaId(),
                article.sources().stream().map(this::toSourceResponse).toList(), article.publishedAt());
    }

    private CategorySummaryResponse toCategoryResponse(CategorySummaryQuery category) {
        if (category == null) {
            return null;
        }

        return new CategorySummaryResponse(category.id(), category.name());
    }

    private Set<TopicSummaryResponse> toTopicResponses(Set<TopicSummaryQuery> topics) {
        return topics.stream()
                .map(this::toTopicResponse)
                .collect(java.util.stream.Collectors.toSet());
    }

    private TopicSummaryResponse toTopicResponse(TopicSummaryQuery topic) {
        return new TopicSummaryResponse(topic.id(), topic.name(), topic.categoryId());
    }

    private SourceResponse toSourceResponse(SourceQueryModel source) {
        return new SourceResponse(source.url(), source.title());
    }
}
