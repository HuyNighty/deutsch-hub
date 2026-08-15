package com.deutschhub.application.content.category.service;

import com.deutschhub.application.content.category.dto.request.CreateCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;
import com.deutschhub.application.content.category.port.in.CreateCategoryUseCase;
import com.deutschhub.application.content.category.port.out.CategoryRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.category.aggregate.Category;
import com.deutschhub.domain.content.category.valueobject.CategoryName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CreateCategoryService implements CreateCategoryUseCase {

    CategoryRepositoryPort categoryRepositoryPort;

    @Override
    public CategoryResponse create(CreateCategoryCommand command) {

        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        CategoryName categoryName = new CategoryName(command.name());

        if (categoryRepositoryPort.existsByName(categoryName)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        Category category = Category.create(categoryName);

        categoryRepositoryPort.save(category);

        return toResponse(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName().value(), category.getStatus());
    }
}
