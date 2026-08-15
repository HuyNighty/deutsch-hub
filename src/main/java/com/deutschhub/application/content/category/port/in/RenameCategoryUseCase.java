package com.deutschhub.application.content.category.port.in;

import com.deutschhub.application.content.category.dto.request.RenameCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;

public interface RenameCategoryUseCase {

    CategoryResponse rename(RenameCategoryCommand command);
}
