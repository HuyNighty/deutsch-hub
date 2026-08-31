package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.query.*;
import com.deutschhub.application.content.article.dto.response.PublishedArticlePageResponse;
import com.deutschhub.application.content.article.dto.response.PublishedArticleSummaryResponse;
import com.deutschhub.application.content.article.port.in.GetPublishedArticlesUseCase;
import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class GetPublishedArticlesService implements GetPublishedArticlesUseCase {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    ArticleQueryPort articleQueryPort;

    @Override
    public PublishedArticlePageResponse getPublishedArticles(GetPublishedArticlesQuery query) {

        int page = normalizePage(query);
        int size = normalizeSize(query);
        String keyword = normalizeKeyword(query);

        PageResult<PublishedArticleQueryModel> result = articleQueryPort.findPublishedArticles(page, size, keyword,
                query == null ? null : query.categoryId(),
                query == null ? null : query.topicId());

        List<PublishedArticleSummaryResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PublishedArticlePageResponse(content, result.totalElements(), result.page(), result.size());
    }

    private String normalizeKeyword(GetPublishedArticlesQuery query) {
        if (query == null || query.keyword() == null || query.keyword().isBlank()) {
            return null;
        }

        return query.keyword().trim();
    }

    private int normalizePage(GetPublishedArticlesQuery query) {
        if (query == null || query.page() < 0) {
            return 0;
        }

        return query.page();
    }

    private int normalizeSize(GetPublishedArticlesQuery query) {
        if (query == null || query.size() <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(query.size(), MAX_PAGE_SIZE);
    }

    private PublishedArticleSummaryResponse toResponse(PublishedArticleQueryModel article) {

        CategorySummaryResponse primaryCategory = toCategoryResponse(article.primaryCategory());

        Set<TopicSummaryResponse> topics = toTopicsResponse(article.topics());

        return new PublishedArticleSummaryResponse(article.articleId(), article.versionId(), article.slug(),
                article.title(), article.summary(), primaryCategory, topics, article.coverMediaId(),
                article.publishedAt());
    }

    private CategorySummaryResponse toCategoryResponse(CategorySummaryQuery categoryQuery) {
        if (categoryQuery == null) {
            return null;
        }

        return new CategorySummaryResponse(categoryQuery.id(), categoryQuery.name());
    }

    private Set<TopicSummaryResponse> toTopicsResponse(Set<TopicSummaryQuery> topicQueries) {
        return topicQueries.stream()
                .map(this::toTopicResponse)
                .collect(Collectors.toSet());
    }

    private TopicSummaryResponse toTopicResponse(TopicSummaryQuery topicQuery) {
        if (topicQuery == null) {
            return null;
        }

        return new TopicSummaryResponse(topicQuery.id(), topicQuery.name(), topicQuery.categoryId());
    }
}
