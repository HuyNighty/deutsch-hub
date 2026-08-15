package com.deutschhub.infrastructure.content.category.persistence.mapper;

import com.deutschhub.domain.content.category.aggregate.Category;
import com.deutschhub.domain.content.category.enums.CategoryStatus;
import com.deutschhub.domain.content.category.valueobject.CategoryName;
import com.deutschhub.infrastructure.content.category.persistence.entity.CategoryJpaEntity;

public class CategoryPersistenceMapper {

    public CategoryJpaEntity toJpa(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryJpaEntity
                .builder()
                .id(category.getId())
                .categoryName(category.getName().value())
                .categoryNameNormalized(category.getName().normalizedValue())
                .categoryStatus(category.getStatus().name())
                .build();
    }

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Category.restore(
                entity.getId(),
                new CategoryName(entity.getCategoryName()),
                CategoryStatus.valueOf(entity.getCategoryStatus())
        );
    }
}
