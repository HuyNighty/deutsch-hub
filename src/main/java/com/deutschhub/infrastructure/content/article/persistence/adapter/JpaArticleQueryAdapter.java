package com.deutschhub.infrastructure.content.article.persistence.adapter;

import com.deutschhub.application.content.article.dto.query.*;
import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.content.article.valueobject.Slug;
import com.deutschhub.domain.shared.valueobject.UserId;
import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleVersionJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.entity.SourceJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.projection.ArticleDetailProjection;
import com.deutschhub.infrastructure.content.article.persistence.projection.PublishedArticleDetailProjection;
import com.deutschhub.infrastructure.content.article.persistence.projection.PublishedArticleProjection;
import com.deutschhub.infrastructure.content.article.persistence.repository.SpringDataArticleRepository;
import com.deutschhub.infrastructure.content.article.persistence.repository.SpringDataArticleVersionRepository;
import com.deutschhub.infrastructure.content.category.persistence.entity.CategoryJpaEntity;
import com.deutschhub.infrastructure.content.category.persistence.repository.SpringDataCategoryRepository;
import com.deutschhub.infrastructure.content.topic.persistence.entity.TopicJpaEntity;
import com.deutschhub.infrastructure.content.topic.persistence.repository.SpringDataTopicRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaArticleQueryAdapter implements ArticleQueryPort {

    SpringDataArticleRepository springDataArticleRepository;
    SpringDataArticleVersionRepository springDataArticleVersionRepository;
    SpringDataCategoryRepository springDataCategoryRepository;
    SpringDataTopicRepository springDataTopicRepository;

    @Override
    public Optional<PublishedArticleDetailQueryModel> findPublishedBySlug(Slug slug) {
        if (slug == null) {
            return Optional.empty();
        }

        return springDataArticleRepository.findPublishedArticleDetailBySlug(slug.value())
                .map(this::toPublishedArticleDetailQueryModel);
    }

    @Override
    public PageResult<PublishedArticleQueryModel> findPublishedArticles(int page, int size, String keyword,
                                                                        UUID categoryId, UUID topicId) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PublishedArticleProjection> result = springDataArticleRepository.findPublishedArticles(keyword, categoryId, topicId, pageable);

        List<PublishedArticleQueryModel> content = result.getContent()
                .stream()
                .map(this::toPublishedArticleQueryModel)
                .toList();

        return new PageResult<>(content, result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Override
    public Optional<ArticleDetailQueryModel> findDetailById(UUID articleId) {
        if (articleId == null) {
            return Optional.empty();
        }

        return springDataArticleRepository.findArticleDetailById(articleId)
                .map(this::toArticleDetailQueryModel);
    }

    @Override
    public boolean isMediaPubliclyReferenced(UUID mediaId) {
        if (mediaId == null) {
            return false;
        }

        return springDataArticleRepository.existsPublishedArticleByCoverMediaId(mediaId);
    }

    private ArticleDetailQueryModel toArticleDetailQueryModel(ArticleDetailProjection projection) {

        ArticleVersionJpaEntity draft = findVersion(projection.draftVersionId());

        ArticleVersionJpaEntity published = findVersion(projection.publishedVersionId());

        return new ArticleDetailQueryModel(projection.articleId(), UserId.of(projection.ownerId()), projection.slug(),
                EditorialStatus.valueOf(projection.editorialStatus()), PublicationStatus.valueOf(projection.publicationStatus()),
                projection.draftVersionId(), projection.publishedVersionId(), projection.createdAt(), projection.publishedAt(),
                projection.archivedAt(), toVersionDetailQueryModel(draft), toVersionDetailQueryModel(published)
        );
    }

    private ArticleVersionJpaEntity findVersion(UUID versionId) {

        if (versionId == null) {
            return null;
        }

        return springDataArticleVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_VERSION_NOT_FOUND));
    }

    private ArticleVersionDetailQueryModel toVersionDetailQueryModel(
            ArticleVersionJpaEntity version
    ) {
        if (version == null) {
            return null;
        }

        List<SourceQueryModel> sources = version.getSources()
                .stream()
                .map(this::toSourceQueryModel)
                .toList();

        return new ArticleVersionDetailQueryModel(version.getId(), version.getVersionNumber(), version.getTitle(),
                version.getSummary(), version.getBody(), version.getPrimaryCategoryId(), new LinkedHashSet<>(version.getTopicIds()),
                version.getCoverMediaId(), sources, version.getCreatedBy(), version.getCreatedAt(), version.getLastModifiedBy(),
                version.getLastModifiedAt()
        );
    }

    private PublishedArticleQueryModel toPublishedArticleQueryModel(PublishedArticleProjection projection) {
        CategorySummaryQuery primaryCategory = findCategorySummary(projection.primaryCategoryId());

        Set<TopicSummaryQuery> topics = findTopicSummaries(projection.versionId());

        return new PublishedArticleQueryModel(projection.articleId(), projection.versionId(), projection.slug(),
                projection.title(), projection.summary(), primaryCategory, topics, projection.coverMediaId(),
                projection.publishedAt());
    }

    private PublishedArticleDetailQueryModel toPublishedArticleDetailQueryModel(PublishedArticleDetailProjection projection) {
        CategorySummaryQuery primaryCategory = findCategorySummary(projection.primaryCategoryId());

        Set<TopicSummaryQuery> topics = findTopicSummaries(projection.versionId());

        List<SourceQueryModel> sources = springDataArticleRepository.findSourcesByVersionId(projection.versionId())
                .stream()
                .map(this::toSourceQueryModel)
                .toList();

        return new PublishedArticleDetailQueryModel(projection.articleId(), projection.versionId(), projection.slug(),
                projection.title(), projection.summary(), projection.body(), primaryCategory, topics,
                projection.coverMediaId(), sources, PublicationStatus.valueOf(projection.publicationStatus()),
                projection.publishedAt());
    }

    private SourceQueryModel toSourceQueryModel(SourceJpaEntity entity) {
        return new SourceQueryModel(entity.getTitle(), entity.getUrl());
    }

    private CategorySummaryQuery findCategorySummary(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }

        CategoryJpaEntity category = springDataCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        return new CategorySummaryQuery(category.getId(), category.getCategoryName()
        );
    }

    private Set<TopicSummaryQuery> findTopicSummaries(UUID versionId) {

        Set<UUID> topicIds = springDataArticleRepository.findTopicIdsByVersionId(versionId);

        if (topicIds == null || topicIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return springDataTopicRepository
                .findByIdIn(topicIds)
                .stream()
                .map(this::toTopicSummaryQuery)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private TopicSummaryQuery toTopicSummaryQuery(TopicJpaEntity topic) {
        return new TopicSummaryQuery(topic.getId(), topic.getCategoryId(), topic.getTopicName());
    }
}
