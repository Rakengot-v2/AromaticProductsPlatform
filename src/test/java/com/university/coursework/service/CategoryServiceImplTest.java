package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.CategoryDTO;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.exception.CategoryNotFoundException;
import com.university.coursework.repository.CategoryRepository;
import com.university.coursework.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceImpl categoryService;

    private UUID categoryId;
    private CategoryEntity categoryEntity;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository);
        categoryId = UUID.randomUUID();

        categoryEntity = new CategoryEntity();
        categoryEntity.setId(categoryId);
        categoryEntity.setName("Electronics");
        categoryEntity.setSlug("electronics");

        categoryDTO = CategoryDTO.builder()
                .id(categoryId)
                .name("Electronics")
                .slug("electronics")
                .build();
    }

    @Test
    void testFindAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(categoryEntity));

        List<CategoryDTO> result = categoryService.findAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void testFindCategoryById() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));

        CategoryDTO result = categoryService.findCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void testFindCategoryByIdNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.findCategoryById(categoryId));
    }

    @Test
    void testCreateCategory() {
        when(categoryRepository.existsBySlug(categoryDTO.getSlug())).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);

        CategoryDTO result = categoryService.createCategory(categoryDTO);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void testUpdateCategory() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);

        CategoryDTO updatedDTO = CategoryDTO.builder()
                .id(categoryId)
                .name("Updated Name")
                .slug("updated-slug")
                .build();

        CategoryDTO result = categoryService.updateCategory(categoryId, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated-slug", result.getSlug());
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void testDeleteCategory() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void testDeleteCategoryNotFound() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(categoryId));
    }
}
