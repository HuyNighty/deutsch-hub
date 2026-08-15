package com.deutschhub.infrastructure.content.article.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Table(name = "articles")
@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "owner_id", nullable = false)
    UUID ownerId;

    @Column(name = "slug", nullable = false, length = 255)
    String slug;

    @Column(name = "editorial_status", nullable = false, length = 30)
    String editorialStatus;

    @Column(name = "publication_status", nullable = false, length = 30)
    String publicationStatus;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "draft_version_id")
    UUID draftVersionId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "published_version_id")
    UUID publishedVersionId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "created_by", nullable = false)
    UUID createdBy;

    @Column(name = "published_at")
    Instant publishedAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "published_by")
    UUID publishedBy;

    @Column(name = "archived_at")
    Instant archivedAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "archived_by")
    UUID archivedBy;

    @Column(name = "ownership_transferred_at")
    Instant ownershipTransferredAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ownership_transferred_by")
    UUID ownershipTransferredBy;
}
