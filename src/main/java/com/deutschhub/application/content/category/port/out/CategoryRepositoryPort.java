package com.deutschhub.application.content.category.port.out;

import com.deutschhub.domain.content.category.aggregate.Category;
import com.deutschhub.domain.content.category.valueobject.CategoryName;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

    boolean existsByName(CategoryName categoryName);

    boolean existsByNameExcludingId(CategoryName categoryName, UUID categoryId);

    void save(Category category);

    Optional<Category> findById(UUID categoryId);
}
