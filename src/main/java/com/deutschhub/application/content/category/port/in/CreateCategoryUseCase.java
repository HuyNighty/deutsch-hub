package com.deutschhub.application.content.category.port.in;

import com.deutschhub.application.content.category.dto.request.CreateCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;

public interface CreateCategoryUseCase {

    CategoryResponse create(CreateCategoryCommand command);
}
