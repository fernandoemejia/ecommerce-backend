package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findAllActive().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(CategoryDto::fromEntityWithSubcategories)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveRootCategories() {
        return categoryRepository.findActiveRootCategories().stream()
                .map(CategoryDto::fromEntityWithSubcategories)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryDto::fromEntityWithSubcategories)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    public CategoryDto createCategory(CategoryDto.CreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }

        Category category = request.toEntity();

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.parentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return CategoryDto.fromEntity(savedCategory);
    }

    public CategoryDto updateCategory(Long id, CategoryDto.UpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.name() != null && !request.name().equals(category.getName())) {
            if (categoryRepository.existsByName(request.name())) {
                throw new DuplicateResourceException("Category", "name", request.name());
            }
            category.setName(request.name());
        }

        if (request.description() != null) {
            category.setDescription(request.description());
        }

        if (request.imageUrl() != null) {
            category.setImageUrl(request.imageUrl());
        }

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.parentId()));
            category.setParent(parent);
        }

        if (request.active() != null) {
            category.setActive(request.active());
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryDto.fromEntity(updatedCategory);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getSubcategories().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }

        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with products");
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategories(String name) {
        return categoryRepository.searchByName(name).stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }
}
