package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.query.ArticleQueryModel;
import com.deutschhub.application.content.article.dto.query.GetPublishedArticlesQuery;
import com.deutschhub.application.content.article.dto.query.PageResult;
import com.deutschhub.application.content.article.dto.response.PublishedArticlePageResponse;
import com.deutschhub.application.content.article.dto.response.PublishedArticleSummaryResponse;
import com.deutschhub.application.content.article.port.in.GetPublishedArticlesUseCase;
import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class GetPublishedArticlesService implements GetPublishedArticlesUseCase {

    ArticleQueryPort articleQueryPort;

    @Override
    public PublishedArticlePageResponse getPublishedArticles(GetPublishedArticlesQuery query) {

        int page = normalizePage(query);
        int size = normalizeSize(query);

        PageResult<ArticleQueryModel> result = articleQueryPort.findPublishedArticles(page, size);

        List<PublishedArticleSummaryResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PublishedArticlePageResponse(content, result.totalElements(), result.page(), result.size());
    }

    private int normalizePage(GetPublishedArticlesQuery query) {
        if (query == null || query.page() < 0) {
            return 0;
        }

        return query.page();
    }

    private int normalizeSize(GetPublishedArticlesQuery query) {
        if (query == null || query.size() < 0) {
            return 20;
        }

        return query.size();
    }

    private PublishedArticleSummaryResponse toResponse(ArticleQueryModel article) {
        return new PublishedArticleSummaryResponse(article.articleId(), article.versionId(), article.slug(),
                article.title(), article.summary(), article.primaryCategoryId(), article.coverMediaId(), article.publishedAt());
    }
}
