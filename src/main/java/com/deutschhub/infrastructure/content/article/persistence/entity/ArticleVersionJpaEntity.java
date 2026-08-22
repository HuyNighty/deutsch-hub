package com.deutschhub.infrastructure.content.article.persistence.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "article_versions")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ArticleVersionJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(name = "version_number", nullable = false)
    Integer versionNumber;

    @Column(name = "title", length = 255)
    String title;

    @Column(name = "summary", length = 2000)
    String summary;

    @Column(name = "body", columnDefinition = "MEDIUMTEXT")
    String body;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "primary_category_id")
    UUID primaryCategoryId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "cover_media_id")
    UUID coverMediaId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "created_by", nullable = false)
    UUID createdBy;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "last_modified_by", nullable = false)
    UUID lastModifiedBy;

    @Column(name = "last_modified_at", nullable = false)
    Instant lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "article_id",
            nullable = false
    )
    ArticleJpaEntity article;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "article_version_sources",
            joinColumns = @JoinColumn(name = "article_version_id")
    )
    @OrderColumn(name = "source_order")
    @Column(nullable = false)
    List<SourceJpaEntity> sources = new ArrayList<>();

    @OneToMany(
            mappedBy = "articleVersion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("submittedAt ASC, id ASC")
    List<ReviewCycleJpaEntity> reviewCycles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "article_version_topics",
            joinColumns = @JoinColumn(
                    name = "article_version_id",
                    nullable = false
            )
    )
    @Column(name = "topic_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    Set<UUID> topicIds = new LinkedHashSet<>();

    public void addReviewCycle(ReviewCycleJpaEntity reviewCycle) {
        if (reviewCycle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_REVIEW_CYCLE_NOT_FOUND);
        }
        reviewCycle.setArticleVersion(this);
        reviewCycles.add(reviewCycle);
    }

    public void replaceReviewCycles(List<ReviewCycleJpaEntity> reviewCycles) {
        this.reviewCycles.clear();

        for (ReviewCycleJpaEntity reviewCycle : reviewCycles) {
            addReviewCycle(reviewCycle);
        }
    }
}