package com.deutschhub.application.content.category.port.in;

import com.deutschhub.application.content.category.dto.request.ReactivateCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;

public interface ReactivateCategoryUseCase {

    CategoryResponse reactivate(ReactivateCategoryCommand command);
}
