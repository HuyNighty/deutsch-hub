package com.deutschhub.application.content.category.service;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.category.port.in.GetActiveCategoriesUseCase;
import com.deutschhub.application.content.category.port.out.CategoryQueryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class GetActiveCategoriesService implements GetActiveCategoriesUseCase {

    CategoryQueryPort categoryQueryPort;

    @Override
    public List<CategorySummaryResponse> getActiveCategories() {
        return categoryQueryPort.findActiveCategories();
    }
}