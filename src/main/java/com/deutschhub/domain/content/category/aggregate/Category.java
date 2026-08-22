package com.deutschhub.domain.content.category.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.category.enums.CategoryStatus;
import com.deutschhub.domain.content.category.valueobject.CategoryName;

import java.util.UUID;

public class Category {

    private UUID id;
    private CategoryName categoryName;
    private CategoryStatus categoryStatus;

    protected Category() {
    }

    public static Category create(CategoryName categoryName) {
        if (categoryName == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }

        Category category = new Category();

        category.id = UUID.randomUUID();
        category.categoryName = categoryName;
        category.categoryStatus = CategoryStatus.ACTIVE;

        return category;
    }

    public static Category restore(UUID id, CategoryName categoryName, CategoryStatus categoryStatus) {
        if (id == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_DATA);
        }

        if (categoryName == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }

        if (categoryStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_STATUS);
        }

        Category category = new Category();

        category.id = id;
        category.categoryName = categoryName;
        category.categoryStatus = categoryStatus;

        return category;
    }

    public void rename(CategoryName newName) {
        if (categoryStatus != CategoryStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NOT_ACTIVE
            );
        }

        if (newName == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }

        this.categoryName = newName;
    }

    public void deactivate() {
        if (categoryStatus != CategoryStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_INACTIVE);
        }

        this.categoryStatus = CategoryStatus.INACTIVE;
    }

    public void reactivate() {
        if (categoryStatus != CategoryStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_ACTIVE);
        }

        this.categoryStatus = CategoryStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public CategoryName getName() {
        return categoryName;
    }

    public CategoryStatus getStatus() {
        return categoryStatus;
    }
}
