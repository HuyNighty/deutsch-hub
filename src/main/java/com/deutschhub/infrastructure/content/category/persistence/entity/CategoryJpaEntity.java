package com.deutschhub.infrastructure.content.category.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CategoryJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @Column(name = "category_name", nullable = false, length = 100)
    String categoryName;

    @Column(name = "category_name_normalized", nullable = false, length = 100)
    String categoryNameNormalized;

    @Column(name = "category_status", nullable = false, length = 20)
    String categoryStatus;
}
