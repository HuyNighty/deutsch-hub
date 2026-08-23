package com.deutschhub.infrastructure.content.article.persistence.repository;

import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.entity.SourceJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.projection.ArticleDetailProjection;
import com.deutschhub.infrastructure.content.article.persistence.projection.PublishedArticleDetailProjection;
import com.deutschhub.infrastructure.content.article.persistence.projection.PublishedArticleProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SpringDataArticleRepository extends JpaRepository<ArticleJpaEntity, UUID> {

    boolean existsBySlug(String slug);

    @Query("""
        SELECT new com.deutschhub.infrastructure.content.article.persistence.projection.PublishedArticleProjection(
            a.id, v.id, a.slug, v.title, v.summary, v.primaryCategoryId, v.coverMediaId, a.publishedAt)
        FROM ArticleJpaEntity a
        JOIN a.versions v
        WHERE a.publishedVersionId = v.id
            AND a.publicationStatus = 'PUBLISHED'
        ORDER BY a.publishedAt DESC
    """)
    Page<PublishedArticleProjection> findPublishedArticles(Pageable pageable);

    @Query("""
        SELECT new com.deutschhub.infrastructure.content.article.persistence.projection.PublishedArticleDetailProjection(
             a.id, v.id, a.slug, v.title, v.summary, v.body, v.primaryCategoryId, v.coverMediaId, a.publicationStatus, a.publishedAt)
        FROM ArticleJpaEntity a
        JOIN a.versions v
        WHERE a.slug = :slug
            AND a.publishedVersionId = v.id
    """)
    Optional<PublishedArticleDetailProjection> findPublishedArticleDetailBySlug(@Param("slug") String slug);

    @Query("""
        SELECT t
        FROM ArticleVersionJpaEntity v
        JOIN v.topicIds t
        WHERE v.id = :versionId
    """)
    Set<UUID> findTopicIdsByVersionId(UUID versionId);

    @Query("""
        SELECT s
        FROM ArticleVersionJpaEntity v
        JOIN v.sources s
        WHERE v.id = :versionId
    """)
    List<SourceJpaEntity> findSourcesByVersionId(UUID versionId);

    @Query("""
        SELECT new com.deutschhub.infrastructure.content.article.persistence.projection.ArticleDetailProjection(
            a.id, a.ownerId, a.slug, a.editorialStatus, a.publicationStatus, a.draftVersionId, a.publishedVersionId, a.createdAt,
            a.publishedAt, a.archivedAt)
        FROM ArticleJpaEntity a
        WHERE a.id = :articleId
    """)
    Optional<ArticleDetailProjection> findArticleDetailById(@Param("articleId") UUID articleId);


    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM ArticleJpaEntity a
        JOIN a.versions v
        WHERE a.publishedVersionId = v.id
            AND a.publicationStatus = 'PUBLISHED'
            AND v.coverMediaId = :mediaId
    """)
    boolean existsPublishedArticleByCoverMediaId(@Param("mediaId") UUID mediaId);
}
