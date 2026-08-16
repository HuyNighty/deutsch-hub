package com.deutschhub.application.content.article.port.out;

import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.valueobject.Slug;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepositoryPort {

    boolean existsBySlug(Slug slug);

    void save(Article article);

    Optional<Article> findById(UUID id);
}
