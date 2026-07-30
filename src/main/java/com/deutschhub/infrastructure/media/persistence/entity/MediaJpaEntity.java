package com.deutschhub.infrastructure.media.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "media",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_media_storage_key",
                        columnNames = "storage_key"
                )
        },
        indexes = {
                @Index(
                        name = "idx_media_uploaded_by",
                         columnList = "uploaded_by"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(name = "original_file_name", nullable = false, length = 255)
    String originalFileName;

    @Column(name = "storage_key", nullable = false, length = 512)
    String storageKey;

    @Column(name = "media_type", nullable = false, length = 20)
    String mediaType;

    @Column(name = "mime_type", nullable = false, length = 255)
    String mimeType;

    @Column(name = "size_bytes", nullable = false)
    long sizeBytes;

    @Column(name = "uploaded_by", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID uploadedBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
}
