package com.deutschhub.infrastructure.learning.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_enrollments_user_course",
                        columnNames = {"user_id", "course_id"}
                )
        },
        indexes = {
                @Index(name = "idx_enrollments_user_id", columnList = "user_id"),
                @Index(name = "idx_enrollments_course_id", columnList = "course_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrollmentJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(name = "user_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID userId;

    @Column(name = "course_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID courseId;

    @Column(nullable = false)
    String status;

    @Column(nullable = false)
    int completedLessons;

    @Column(nullable = false)
    int totalLessons;

    @Column(nullable = false)
    BigDecimal completionPercentage;

    @Column(nullable = false)
    int totalStudyMinutes;

    LocalDateTime progressLastUpdatedAt;

    LocalDateTime enrolledAt;
    LocalDateTime completedAt;
    LocalDateTime droppedAt;
    LocalDateTime expiredAt;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
}
