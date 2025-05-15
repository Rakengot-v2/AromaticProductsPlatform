package com.university.coursework.service.impl;

import com.university.coursework.domain.CategoryDTO;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.exception.CategoryNotFoundException;
import com.university.coursework.repository.CategoryRepository;
import com.university.coursework.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> findAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO findCategoryById(UUID id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        return mapToDto(category);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsBySlug(categoryDTO.getSlug())) {
            throw new RuntimeException("Category with this slug already exists");
        }

        CategoryEntity category = mapToEntity(categoryDTO);
        CategoryEntity savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    @Override
    public CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO) {
        CategoryEntity existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        if (!existingCategory.getSlug().equals(categoryDTO.getSlug()) &&
                categoryRepository.existsBySlug(categoryDTO.getSlug())) {
            throw new RuntimeException("Category with this slug already exists");
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setSlug(categoryDTO.getSlug());

        CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        return mapToDto(updatedCategory);
    }

    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO mapToDto(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .build();
    }

    private CategoryEntity mapToEntity(CategoryDTO dto) {
        return CategoryEntity.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .build();
    }
}