package io.github.rohergun.budgetmanager.category;

import io.github.rohergun.budgetmanager.category.dto.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
