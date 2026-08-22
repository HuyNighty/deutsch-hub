package com.deutschhub.infrastructure.content.article.web.controller;

import com.deutschhub.application.content.article.port.in.GetPublishedArticleBySlugUseCase;
import com.deutschhub.application.content.article.port.in.GetPublishedArticlesUseCase;
import com.deutschhub.application.content.article.port.in.PublishArticleUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/articles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleController {

    GetPublishedArticleBySlugUseCase getPublishedArticleBySlugUseCase;
    GetPublishedArticlesUseCase getPublishedArticlesUseCase;
    PublishArticleUseCase publishArticleUseCase;
}
