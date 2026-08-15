package com.deutschhub.application.content.category.port.in;

import com.deutschhub.application.content.category.dto.request.DeactivateCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;

public interface DeactivateCategoryUseCase {

    CategoryResponse deactivate(DeactivateCategoryCommand command);
}
