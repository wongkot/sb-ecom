package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty())
            throw new APIException("No categories created.");

        return categories;
    }

    @Override
    public void createCategory(Category category) {
        if (categoryRepository.existsByCategoryName(category.getCategoryName()))
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !");

        categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long categoryId, Category category) {
        if (! categoryRepository.existsById(categoryId))
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        if (categoryRepository.existsByCategoryIdNotAndCategoryName(categoryId, category.getCategoryName()))
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !");

        // Set ID of the input category (do this to avoid having to set each field on the existing category data)
        category.setCategoryId(categoryId);

        return categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);
        return "Category with categoryId: " + categoryId + " deleted successfully";
    }
}
