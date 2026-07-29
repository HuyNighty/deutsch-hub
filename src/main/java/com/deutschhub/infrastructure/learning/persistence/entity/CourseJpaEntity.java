package com.deutschhub.infrastructure.learning.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(nullable = false)
    String title;

    @Column(length = 2000)
    String description;

    @Column(nullable = false, length = 3)
    String level;

    @Column(nullable = false)
    BigDecimal priceAmount;

    @Column(nullable = false, length = 3)
    String priceCurrency;

    boolean published;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID instructorId;

    int estimatedHours;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    List<SectionJpaEntity> sections = new ArrayList<>();
}
