package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.CategoryRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;


    private UUID userId;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        CategoryEntity category = categoryRepository.save(CategoryEntity.builder()
                .name("Electronics")
                .slug("electronics")
                .build());
        categoryId = category.getId();

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("iPhone 14")
                .description("Latest Apple smartphone with A16 Bionic chip")
                .price(BigDecimal.valueOf(1099))
                .stock(5)
                .isActive(true)
                .category(category)
                .imageUrl("https://example.com/iphone14.jpg")
                .createdAt(LocalDateTime.now())
                .build());
        productId = product.getId();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build());

        userId = user.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldCreateNewProduct() {
        ProductDTO newProduct = new ProductDTO("MacBook Air M2", "Lightweight Apple laptop",
                BigDecimal.valueOf(1299), 5, "https://example.com/macbook.jpg", categoryId,
                true, LocalDateTime.now());

        webTestClient.post()
                .uri("/api/products")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductDTO.class)
                .consumeWith(response -> {
                    ProductDTO product = response.getResponseBody();
                    assertNotNull(product);
                    assertEquals("MacBook Air M2", product.getName());
                    assertEquals("Lightweight Apple laptop", product.getDescription());
                    assertEquals(BigDecimal.valueOf(1299), product.getPrice());
                    assertEquals("https://example.com/macbook.jpg", product.getImageUrl());
                });
    }

    @Test
    void shouldUpdateProductDetails() {
        ProductDTO updatedProduct = new ProductDTO("iPhone 14 Pro", "Advanced Apple smartphone",
                BigDecimal.valueOf(1199), 6, "https://example.com/iphone14pro.jpg", categoryId,
                true, LocalDateTime.now());

        webTestClient.put()
                .uri("/api/products/{productId}", productId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDTO.class)
                .consumeWith(response -> {
                    ProductDTO product = response.getResponseBody();
                    assertNotNull(product);
                    assertEquals("iPhone 14 Pro", product.getName());
                    assertEquals("Advanced Apple smartphone", product.getDescription());
                    assertEquals(BigDecimal.valueOf(1199), product.getPrice());
                    assertEquals("https://example.com/iphone14pro.jpg", product.getImageUrl());
                });
    }

    @Test
    void shouldDeleteProduct() {
        webTestClient.delete()
                .uri("/api/products/{productId}", productId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNoContent();

        assertFalse(productRepository.existsById(productId), "Product should be deleted.");
    }

    @Test
    void shouldReturnNotFoundForUpdatingNonexistentProduct() {
        UUID nonexistentProductId = UUID.randomUUID();

        ProductDTO updatedProduct = new ProductDTO("iPhone 15", "Future Apple phone",
                BigDecimal.valueOf(1399), 9, "https://example.com/iphone15.jpg", categoryId,
                true, LocalDateTime.now());

        webTestClient.put()
                .uri("/api/products/{productId}", nonexistentProductId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }

    @Test
    void shouldReturnNotFoundForDeletingNonexistentProduct() {
        UUID nonexistentProductId = UUID.randomUUID();

        webTestClient.delete()
                .uri("/api/products/{productId}", nonexistentProductId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }
}