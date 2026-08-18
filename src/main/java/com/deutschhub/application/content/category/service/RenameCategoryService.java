package com.deutschhub.application.content.category.service;

import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.content.category.dto.request.RenameCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;
import com.deutschhub.application.content.category.port.in.RenameCategoryUseCase;
import com.deutschhub.application.content.category.port.out.CategoryRepositoryPort;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
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
public class RenameCategoryService implements RenameCategoryUseCase {

    CategoryRepositoryPort categoryRepositoryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;

    @Override
    public CategoryResponse rename(RenameCategoryCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        authorizationPolicy.requireAdmin(actor);

        Category category = categoryRepositoryPort.findById(command.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        CategoryName newName = new CategoryName(command.name());

        if (category.getName().normalizedValue().equals(newName.normalizedValue())) {
            return toResponse(category);
        }

        if (categoryRepositoryPort.existsByNameExcludingId(newName, category.getId())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        category.rename(newName);

        categoryRepositoryPort.save(category);

        return toResponse(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName().value(), category.getStatus());
    }
}
