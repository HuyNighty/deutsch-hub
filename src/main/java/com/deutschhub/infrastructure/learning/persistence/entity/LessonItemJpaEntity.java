package com.deutschhub.infrastructure.learning.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lesson_items",
        indexes = {
                @Index(name = "idx_lesson_item_lesson_id", columnList = "lesson_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonItemJpaEntity {


    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(nullable = false)
    String type;

    @Column(nullable = false)
    String title;

    @Column(length = 1000)
    String description;

    @Column(columnDefinition = "TEXT")
    String content;

    String resourceUrl;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID quizId;

    @Column(nullable = false)
    int estimatedMinutes;

    @Column(nullable = false)
    int orderIndex;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    LessonJpaEntity lesson;
}
