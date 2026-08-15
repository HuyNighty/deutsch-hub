package com.deutschhub.infrastructure.content.article.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_cycles")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class ReviewCycleJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "submitted_by", nullable = false)
    UUID submittedBy;

    @Column(name = "submitted_at", nullable = false)
    Instant submittedAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "reviewed_by")
    UUID reviewedBy;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "withdrawn_by")
    UUID withdrawnBy;

    @Column(name = "withdrawn_at")
    Instant withdrawnAt;

    @Column(name = "result", nullable = false, length = 30)
    String result;

    @Column(name = "feedback", columnDefinition = "TEXT")
    String feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "article_version_id",
            nullable = false
    )
    ArticleVersionJpaEntity articleVersion;
}