package com.university.coursework.service;

import com.university.coursework.domain.ProductDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(UUID id);
    List<ProductDTO> searchProducts(String name, String category, BigDecimal minPrice, BigDecimal maxPrice);
    List<ProductDTO> findByCategoryId(UUID categoryId);
    List<ProductDTO> findAll();
    ProductDTO findById(UUID id);
    List<ProductDTO> getProductsByCategory(UUID categoryId);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(UUID id, ProductDTO productDTO);
    void deleteProduct(UUID id);
}