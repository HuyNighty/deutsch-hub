package com.deutschhub.application.content.category.port.in;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;

import java.util.List;

public interface GetActiveCategoriesUseCase {

    List<CategorySummaryResponse> getActiveCategories();
}
