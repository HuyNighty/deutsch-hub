package com.deutschhub.infrastructure.content.category.persistence.adapter;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.category.port.out.CategoryQueryPort;
import com.deutschhub.domain.content.category.enums.CategoryStatus;
import com.deutschhub.infrastructure.content.category.persistence.entity.CategoryJpaEntity;
import com.deutschhub.infrastructure.content.category.persistence.repository.SpringDataCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaCategoryQueryAdapter implements CategoryQueryPort {

    SpringDataCategoryRepository springDataCategoryRepository;

    @Override
    public List<CategorySummaryResponse> findActiveCategories() {
        return springDataCategoryRepository.findByCategoryStatus(CategoryStatus.ACTIVE.name())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public boolean existsActiveCategory(UUID categoryId) {
        if (categoryId == null) {
            return false;
        }

        return springDataCategoryRepository.existsByIdAndCategoryStatus(categoryId, CategoryStatus.ACTIVE.name());
    }

    private CategorySummaryResponse toResponse(CategoryJpaEntity category) {
        return new CategorySummaryResponse(category.getId(), category.getCategoryName());
    }
}
