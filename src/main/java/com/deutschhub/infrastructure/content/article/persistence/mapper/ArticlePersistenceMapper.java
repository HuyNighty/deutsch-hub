package com.deutschhub.infrastructure.content.article.persistence.mapper;

import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.entity.ArticleVersion;
import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.content.article.valueobject.Slug;
import com.deutschhub.domain.shared.valueobject.UserId;
import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleVersionJpaEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticlePersistenceMapper {

    ArticleVersionPersistenceMapper articleVersionPersistenceMapper;

    public ArticleJpaEntity toJpa(Article article) {
        if (article == null) {
            return null;
        }

        ArticleJpaEntity entity = ArticleJpaEntity
                .builder()
                .id(article.getId())
                .ownerId(article.getOwnerId().value())
                .slug(article.getSlug().value())
                .editorialStatus(article.getEditorialStatus().name())
                .publicationStatus(article.getPublicationStatus().name())
                .draftVersionId(article.getDraftVersionId())
                .publishedVersionId(article.getPublishedVersionId())
                .createdAt(article.getCreatedAt())
                .createdBy(article.getCreatedBy().value())
                .publishedAt(article.getPublishedAt())
                .publishedBy(toUuid(article.getPublishedBy()))
                .archivedAt(article.getArchivedAt())
                .archivedBy(toUuid(article.getArchivedBy()))
                .ownershipTransferredAt(article.getOwnershipTransferredAt())
                .ownershipTransferredBy(toUuid(article.getOwnershipTransferredBy()))
                .build();

        addVersions(entity, article);

        return entity;
    }

    public Article toDomain(ArticleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Article.restore(
                entity.getId(),
                UserId.of(entity.getOwnerId()),
                new Slug(entity.getSlug()),
                EditorialStatus.valueOf(entity.getEditorialStatus()),
                PublicationStatus.valueOf(entity.getPublicationStatus()),
                entity.getDraftVersionId(),
                entity.getPublishedVersionId(),
                toVersions(entity.getVersions()),
                entity.getCreatedAt(),
                UserId.of(entity.getCreatedBy()),
                entity.getPublishedAt(),
                toUserId(entity.getPublishedBy()),
                entity.getArchivedAt(),
                toUserId(entity.getArchivedBy()),
                toUserId(entity.getOwnershipTransferredBy()),
                entity.getOwnershipTransferredAt()
        );
    }

    public void updateJpa(ArticleJpaEntity entity, Article article) {

        if (entity == null || article == null) {
            return;
        }

        entity.setOwnerId(article.getOwnerId().value());
        entity.setSlug(article.getSlug().value());
        entity.setEditorialStatus(article.getEditorialStatus().name());
        entity.setPublicationStatus(article.getPublicationStatus().name());

        entity.setDraftVersionId(article.getDraftVersionId());
        entity.setPublishedVersionId(article.getPublishedVersionId());

        entity.setPublishedAt(article.getPublishedAt());
        entity.setPublishedBy(toUuid(article.getPublishedBy()));

        entity.setArchivedAt(article.getArchivedAt());
        entity.setArchivedBy(toUuid(article.getArchivedBy()));

        entity.setOwnershipTransferredAt(article.getOwnershipTransferredAt());

        entity.setOwnershipTransferredBy(toUuid(article.getOwnershipTransferredBy()));

        updateVersions(entity, article);
    }

    private void updateVersions(ArticleJpaEntity entity, Article article) {
        for (ArticleVersion version : article.getVersions()) {

            ArticleVersionJpaEntity existingVersion = entity.getVersions()
                    .stream()
                    .filter(item -> item.getId().equals(version.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingVersion != null) {
                articleVersionPersistenceMapper.updateJpa(existingVersion, version);
                continue;
            }

            ArticleVersionJpaEntity newVersion = articleVersionPersistenceMapper.toJpa(version);

            entity.addVersion(newVersion);
        }
    }

    private void addVersions(ArticleJpaEntity entity, Article article) {
        for (ArticleVersion version : article.getVersions()) {
            ArticleVersionJpaEntity versionEntity = articleVersionPersistenceMapper.toJpa(version);
            entity.addVersion(versionEntity);
        }
    }

    private UUID toUuid(UserId userId) {
        return userId == null ? null : userId.value();
    }

    private List<ArticleVersion> toVersions(List<ArticleVersionJpaEntity> versions) {
        if (versions == null) {
            return List.of();
        }

        return versions.stream()
                .map(articleVersionPersistenceMapper::toDomain)
                .toList();
    }

    private UserId toUserId(UUID value) {
        return value == null ? null : UserId.of(value);
    }
}
