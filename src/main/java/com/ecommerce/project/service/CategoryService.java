package com.ecommerce.project.service;

import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;

public interface CategoryService {
    CategoryResponse getAllCategories();
    CategoryDTO createCategory(CategoryDTO category);
    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
    CategoryDTO deleteCategory(Long categoryId);
}
