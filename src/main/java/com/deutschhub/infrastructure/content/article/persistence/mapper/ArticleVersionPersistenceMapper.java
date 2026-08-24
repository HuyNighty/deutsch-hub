package com.deutschhub.infrastructure.content.article.persistence.mapper;

import com.deutschhub.domain.content.article.entity.ArticleVersion;
import com.deutschhub.domain.content.article.entity.ReviewCycle;
import com.deutschhub.domain.content.article.valueobject.ArticleTitle;
import com.deutschhub.domain.content.article.valueobject.Body;
import com.deutschhub.domain.content.article.valueobject.Source;
import com.deutschhub.domain.content.article.valueobject.Summary;
import com.deutschhub.domain.content.article.valueobject.VersionNumber;
import com.deutschhub.domain.shared.valueobject.UserId;
import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleVersionJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.entity.SourceJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.entity.ReviewCycleJpaEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleVersionPersistenceMapper {

    ReviewCyclePersistenceMapper reviewCyclePersistenceMapper;

    public ArticleVersionJpaEntity toJpa(ArticleVersion articleVersion) {
        if (articleVersion == null) {
            return null;
        }

        ArticleVersionJpaEntity entity = ArticleVersionJpaEntity
                .builder()
                .id(articleVersion.getId())
                .versionNumber(articleVersion.getVersionNumber().value())
                .title(toValue(articleVersion.getTitle()))
                .summary(toValue(articleVersion.getSummary()))
                .body(toValue(articleVersion.getBody()))
                .primaryCategoryId(articleVersion.getPrimaryCategoryId())
                .coverMediaId(articleVersion.getCoverMediaId())
                .createdBy(articleVersion.getCreatedBy().value())
                .createdAt(articleVersion.getCreatedAt())
                .lastModifiedBy(articleVersion.getLastModifiedBy().value())
                .lastModifiedAt(articleVersion.getLastModifiedAt())
                .build();

        entity.setTopicIds(toTopicIds(articleVersion.getTopicIds()));
        entity.setSources(toSourcesJpa(articleVersion.getSources()));

        addReviewCycles(entity, articleVersion.getReviewCycles());

        return entity;
    }

    public ArticleVersion toDomain(ArticleVersionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ArticleVersion.restore(
                entity.getId(),
                new VersionNumber(entity.getVersionNumber()),
                toArticleTitle(entity.getTitle()),
                toSummary(entity.getSummary()),
                toBody(entity.getBody()),
                entity.getPrimaryCategoryId(),
                toTopicIds(entity.getTopicIds()),
                entity.getCoverMediaId(),
                toSources(entity.getSources()),
                toReviewCycles(entity.getReviewCycles()),
                UserId.of(entity.getCreatedBy()),
                entity.getCreatedAt(),
                UserId.of(entity.getLastModifiedBy()),
                entity.getLastModifiedAt()
        );
    }

    public void updateJpa(ArticleVersionJpaEntity entity, ArticleVersion version) {
        if (version == null || entity == null) {
            return;
        }

        entity.setVersionNumber(version.getVersionNumber().value());
        entity.setTitle(toValue(version.getTitle()));
        entity.setSummary(toValue(version.getSummary()));
        entity.setBody(toValue(version.getBody()));

        entity.setPrimaryCategoryId(version.getPrimaryCategoryId());
        entity.setCoverMediaId(version.getCoverMediaId());

        entity.getTopicIds().clear();
        entity.getTopicIds().addAll(toTopicIds(version.getTopicIds()));

        entity.getSources().clear();
        entity.getSources().addAll(toSourcesJpa(version.getSources()));

        entity.setLastModifiedBy(version.getLastModifiedBy().value());
        entity.setLastModifiedAt(version.getLastModifiedAt());

        updateReviewCycles(entity, version.getReviewCycles());
    }

    private void updateReviewCycles(ArticleVersionJpaEntity entity, List<ReviewCycle> domainReviewCycles) {

        for (ReviewCycle domainCycle : domainReviewCycles) {
            ReviewCycleJpaEntity existingEntity = entity.getReviewCycles()
                    .stream()
                    .filter(jpaCycle -> jpaCycle.getId().equals(domainCycle.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingEntity != null) {
                reviewCyclePersistenceMapper.updateJpa(existingEntity, domainCycle);
            } else {
                ReviewCycleJpaEntity newEntity = reviewCyclePersistenceMapper.toJpa(domainCycle);

                entity.addReviewCycle(newEntity);
            }
        }
    }

    private void addReviewCycles(ArticleVersionJpaEntity entity, List<ReviewCycle> reviewCycles) {
        for (ReviewCycle reviewCycle : reviewCycles) {
            ReviewCycleJpaEntity reviewCycleEntity = reviewCyclePersistenceMapper.toJpa(reviewCycle);
            entity.addReviewCycle(reviewCycleEntity);
        }
    }

    private String toValue(ArticleTitle title) {
        return title == null ? null : title.value();
    }

    private String toValue(Summary summary) {
        return summary == null ? null : summary.value();
    }

    private String toValue(Body body) {
        return body == null ? null : body.value();
    }

    private ArticleTitle toArticleTitle(String value) {
        return value == null ? null : new ArticleTitle(value);
    }

    private Summary toSummary(String value) {
        return value == null ? null : new Summary(value);
    }

    private Body toBody(String value) {
        return value == null ? null : new Body(value);
    }

    private Set<UUID> toTopicIds(Set<UUID> topicIds) {
        if (topicIds == null) {
            return new LinkedHashSet<>();
        }

        return new LinkedHashSet<>(topicIds);
    }

    private List<SourceJpaEntity> toSourcesJpa(List<Source> sources) {
        if (sources == null) {
            return List.of();
        }

        return sources.stream()
                .map(this::toSourceJpaEntity)
                .toList();
    }

    private SourceJpaEntity toSourceJpaEntity(Source source) {
        if (source == null) {
            return null;
        }

        return SourceJpaEntity.builder()
                .title(source.title())
                .url(source.url())
                .build();
    }

    private List<Source> toSources(List<SourceJpaEntity> sources) {
        if (sources == null) {
            return List.of();
        }

        return sources.stream()
                .map(this::toSource)
                .toList();
    }

    private Source toSource(SourceJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Source(
                entity.getTitle(),
                entity.getUrl()
        );
    }

    private List<ReviewCycle> toReviewCycles(List<ReviewCycleJpaEntity> reviewCycles) {
        if (reviewCycles == null) {
            return List.of();
        }

        return reviewCycles.stream()
                .map(reviewCyclePersistenceMapper::toDomain)
                .toList();
    }
}