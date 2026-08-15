package com.deutschhub.application.content.category.service;

import com.deutschhub.application.content.category.dto.request.ReactivateCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;
import com.deutschhub.application.content.category.port.in.ReactivateCategoryUseCase;
import com.deutschhub.application.content.category.port.out.CategoryRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.category.aggregate.Category;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ReactivateCategoryService implements ReactivateCategoryUseCase {

    CategoryRepositoryPort categoryRepositoryPort;

    @Override
    public CategoryResponse reactivate(ReactivateCategoryCommand command) {
        if (command == null || command.categoryId() == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        Category category = categoryRepositoryPort.findById(command.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        category.reactivate();

        categoryRepositoryPort.save(category);

        return toResponse(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName().value(), category.getStatus());
    }
}