package com.deutschhub.infrastructure.learning.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    String level;

    @Column(nullable = false)
    BigDecimal priceAmount;

    @Column(nullable = false)
    String priceCurrency;

    boolean published;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID instructorId;

    int estimatedHours;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;
}
