package com.deutschhub.infrastructure.content.article.persistence.repository;

import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleVersionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataArticleVersionRepository extends JpaRepository<ArticleVersionJpaEntity, UUID> {
}
