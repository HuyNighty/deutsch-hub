package com.deutschhub.infrastructure.content.article.web.controller;

import com.deutschhub.application.content.article.dto.query.GetPublishedArticlesQuery;
import com.deutschhub.application.content.article.dto.response.PublishedArticleDetailResponse;
import com.deutschhub.application.content.article.dto.response.PublishedArticlePageResponse;
import com.deutschhub.application.content.article.port.in.GetPublishedArticleBySlugUseCase;
import com.deutschhub.application.content.article.port.in.GetPublishedArticlesUseCase;
import com.deutschhub.common.util.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/articles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleController {

    GetPublishedArticleBySlugUseCase getPublishedArticleBySlugUseCase;
    GetPublishedArticlesUseCase getPublishedArticlesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PublishedArticlePageResponse>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID topicId
    ) {
        GetPublishedArticlesQuery query = new GetPublishedArticlesQuery(page, size, keyword, categoryId, topicId);

        PublishedArticlePageResponse response = getPublishedArticlesUseCase.getPublishedArticles(query);

        return ResponseEntity.ok(
                ApiResponse.<PublishedArticlePageResponse>builder()
                        .message("Get published articles successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PublishedArticleDetailResponse>> getPublishedArticleBySlug(@PathVariable String slug) {
        PublishedArticleDetailResponse response = getPublishedArticleBySlugUseCase.getBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.<PublishedArticleDetailResponse>builder()
                        .message("Get published article by slug successfully")
                        .result(response)
                        .build()
        );
    }
}
