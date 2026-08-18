package com.deutschhub.application.content.category.port.out;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;

import java.util.List;

public interface CategoryQueryPort {

    List<CategorySummaryResponse> findActiveCategories();
}
