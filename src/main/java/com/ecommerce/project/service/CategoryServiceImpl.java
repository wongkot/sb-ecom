package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.PaginationResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PaginationService paginationService;

    @Override
    public PaginationResponse<CategoryDTO> getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageDetails = paginationService.getPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();
        if (categories.isEmpty())
            throw new APIException("No categories created.");

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        return paginationService.getPaginationResponse(categoryDTOs, categoryPage);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        if (categoryRepository.existsByCategoryName(category.getCategoryName()))
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !");

        Category savedCategory = categoryRepository.save(category);

        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        if (! categoryRepository.existsById(categoryId))
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        Category category = modelMapper.map(categoryDTO, Category.class);
        if (categoryRepository.existsByCategoryIdNotAndCategoryName(categoryId, category.getCategoryName()))
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !");

        // Set ID of the input category (do this to avoid having to set each field on the existing category data)
        category.setCategoryId(categoryId);
        Category updatedCategory = categoryRepository.save(category);

        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);
        return modelMapper.map(category, CategoryDTO.class);
    }
}
