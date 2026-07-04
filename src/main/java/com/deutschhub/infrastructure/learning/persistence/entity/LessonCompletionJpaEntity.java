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
        name = "lesson_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_completions_enrollment_lesson",
                        columnNames = {"enrollment_id", "lesson_id"}
                )
        },
        indexes = {
                @Index(name = "idx_lesson_completions_enrollment_id", columnList = "enrollment_id"),
                @Index(name = "idx_lesson_completions_lesson_id", columnList = "lesson_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonCompletionJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(name = "enrollment_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID enrollmentId;

    @Column(name = "lesson_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID lessonId;

    LocalDateTime completedAt;
}
