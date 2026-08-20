package com.deutschhub.infrastructure.content.category.persistence.repository;

import com.deutschhub.infrastructure.content.category.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    List<CategoryJpaEntity> findByCategoryStatus(String categoryStatus);

    boolean existsByCategoryNameNormalized(String categoryNameNormalized);

    boolean existsByCategoryNameNormalizedAndIdNot(String categoryNameNormalized, UUID id);

    boolean existsByIdAndCategoryStatus(UUID id, String categoryStatus);
}
