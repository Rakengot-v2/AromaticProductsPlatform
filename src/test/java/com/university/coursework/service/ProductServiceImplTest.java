package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.CategoryRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private ProductServiceImpl productService;

    private UUID productId;
    private UUID categoryId;
    private ProductEntity productEntity;
    private ProductDTO productDTO;
    private CategoryEntity categoryEntity;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, categoryRepository);
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        categoryEntity = new CategoryEntity();
        categoryEntity.setId(categoryId);
        categoryEntity.setName("Electronics");

        productEntity = new ProductEntity();
        productEntity.setId(productId);
        productEntity.setName("Smartphone");
        productEntity.setDescription("Latest model");
        productEntity.setPrice(BigDecimal.valueOf(799.99));
        productEntity.setStock(10);
        productEntity.setCategory(categoryEntity);
        productEntity.setIsActive(true);

        productDTO = ProductDTO.builder()
                .name("Smartphone")
                .description("Latest model")
                .price(BigDecimal.valueOf(799.99))
                .stock(10)
                .categoryId(categoryId)
                .isActive(true)
                .build();
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(productEntity));

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        ProductDTO result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals("Smartphone", result.getName());
        verify(productRepository).findById(productId);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void testFindByCategoryId() {
        when(productRepository.findByCategoryId(categoryId)).thenReturn(List.of(productEntity));

        List<ProductDTO> result = productService.findByCategoryId(categoryId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByCategoryId(categoryId);
    }

    @Test
    void testCreateProduct() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

        ProductDTO result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals("Smartphone", result.getName());
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void testUpdateProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

        ProductDTO updatedDTO = ProductDTO.builder()
                .name("Updated Smartphone")
                .description("New version")
                .price(BigDecimal.valueOf(899.99))
                .stock(15)
                .categoryId(categoryId)
                .isActive(true)
                .build();

        ProductDTO result = productService.updateProduct(productId, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Smartphone", result.getName());
        assertEquals(BigDecimal.valueOf(899.99), result.getPrice());
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleteProduct(productId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void testDeleteProductNotFound() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId));
    }
}
