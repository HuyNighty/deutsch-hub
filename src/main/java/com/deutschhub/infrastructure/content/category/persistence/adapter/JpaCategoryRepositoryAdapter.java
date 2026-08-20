package com.deutschhub.infrastructure.content.category.persistence.adapter;

import com.deutschhub.application.content.category.port.out.CategoryRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.category.aggregate.Category;
import com.deutschhub.domain.content.category.valueobject.CategoryName;
import com.deutschhub.infrastructure.content.category.persistence.entity.CategoryJpaEntity;
import com.deutschhub.infrastructure.content.category.persistence.mapper.CategoryPersistenceMapper;
import com.deutschhub.infrastructure.content.category.persistence.repository.SpringDataCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaCategoryRepositoryAdapter implements CategoryRepositoryPort {

    SpringDataCategoryRepository springDataCategoryRepository;
    CategoryPersistenceMapper categoryPersistenceMapper;

    @Override
    public boolean existsByName(CategoryName categoryName) {
        if (categoryName == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }

        return springDataCategoryRepository.existsByCategoryNameNormalized(categoryName.normalizedValue());
    }

    @Override
    public boolean existsByNameExcludingId(CategoryName categoryName, UUID categoryId) {
        if (categoryName == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }

        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        return springDataCategoryRepository.existsByCategoryNameNormalizedAndIdNot(categoryName.normalizedValue(), categoryId);
    }

    @Override
    public void save(Category category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        CategoryJpaEntity entity = categoryPersistenceMapper.toJpa(category);

        springDataCategoryRepository.save(entity);
    }

    @Override
    public Optional<Category> findById(UUID categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        return springDataCategoryRepository.findById(categoryId).map(categoryPersistenceMapper::toDomain);
    }
}
