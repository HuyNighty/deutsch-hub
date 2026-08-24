package com.deutschhub.infrastructure.content.category.web.controller;

import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.category.port.in.GetActiveCategoriesUseCase;
import com.deutschhub.common.util.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    GetActiveCategoriesUseCase getActiveCategoriesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategories() {

        List<CategorySummaryResponse> responses = getActiveCategoriesUseCase.getActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.<List<CategorySummaryResponse>>builder()
                        .message("Get categories successfully")
                        .result(responses)
                        .build()
        );
    }
}
