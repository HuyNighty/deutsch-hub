package com.deutschhub.infrastructure.content.category.web.controller;

import com.deutschhub.application.content.category.dto.request.CreateCategoryCommand;
import com.deutschhub.application.content.category.dto.request.DeactivateCategoryCommand;
import com.deutschhub.application.content.category.dto.request.ReactivateCategoryCommand;
import com.deutschhub.application.content.category.dto.request.RenameCategoryCommand;
import com.deutschhub.application.content.category.dto.response.CategoryResponse;
import com.deutschhub.application.content.category.dto.response.CategorySummaryResponse;
import com.deutschhub.application.content.category.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.content.category.web.request.CreateCategoryRequest;
import com.deutschhub.infrastructure.content.category.web.request.RenameCategoryRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/admin/categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {

    CreateCategoryUseCase createCategoryUseCase;
    GetActiveCategoriesUseCase getActiveCategoriesUseCase;
    DeactivateCategoryUseCase deactivateCategoryUseCase;
    ReactivateCategoryUseCase reactivateCategoryUseCase;
    RenameCategoryUseCase renameCategoryUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryCommand command = new CreateCategoryCommand(request.categoryName());

        CategoryResponse response = createCategoryUseCase.create(command);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Create category successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getActiveCategories() {

        List<CategorySummaryResponse> responses = getActiveCategoriesUseCase.getActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.<List<CategorySummaryResponse>>builder()
                        .message("Get active categories successfully")
                        .result(responses)
                        .build()
        );
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> renameCategory(
            @PathVariable UUID categoryId,
            @RequestBody @Valid RenameCategoryRequest request
    ) {
        RenameCategoryCommand command = new RenameCategoryCommand(categoryId, request.categoryName());

        CategoryResponse response = renameCategoryUseCase.rename(command);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Rename category successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{categoryId}/deactivate")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivateCategory(@PathVariable UUID categoryId) {

        DeactivateCategoryCommand command = new DeactivateCategoryCommand(categoryId);

        CategoryResponse response = deactivateCategoryUseCase.deactivate(command);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Deactivate category successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{categoryId}/reactivate")
    public ResponseEntity<ApiResponse<CategoryResponse>> reactivateCategory(@PathVariable UUID categoryId) {

        ReactivateCategoryCommand command = new ReactivateCategoryCommand(categoryId);

        CategoryResponse response = reactivateCategoryUseCase.reactivate(command);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Reactivate category successfully")
                        .result(response)
                        .build()
        );
    }
}
