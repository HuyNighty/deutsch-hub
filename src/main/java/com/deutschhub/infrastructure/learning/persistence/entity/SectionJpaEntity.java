package com.deutschhub.infrastructure.learning.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(nullable = false)
    String title;

    @Column(length = 1000)
    String description;

    @Column(nullable = false)
    int orderIndex;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    CourseJpaEntity course;

}
