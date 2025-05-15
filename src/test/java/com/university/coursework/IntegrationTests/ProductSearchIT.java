package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.repository.CategoryRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductSearchIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        CategoryEntity category = categoryRepository.save(CategoryEntity.builder()
                .name("Electronics")
                .slug("electronics")
                .build());

        productRepository.save(ProductEntity.builder()
                .name("iPhone 13")
                .price(BigDecimal.valueOf(999))
                .description("Latest iPhone model")
                .stock(5)
                .imageUrl("https://example.com/iphone13.jpg")
                .isActive(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .build());

        productRepository.save(ProductEntity.builder()
                .name("Samsung Galaxy S22")
                .price(BigDecimal.valueOf(899))
                .description("Latest Samsung model")
                .stock(3)
                .imageUrl("https://example.com/galaxyS22.jpg")
                .isActive(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    void shouldSearchProductsByName() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/products/search")
                        .queryParam("name", "iPhone")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDTO.class)
                .consumeWith(response -> {
                    List<ProductDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertFalse(products.isEmpty());
                    assertFalse(products.stream().noneMatch(p -> p.getName().contains("iPhone")));
                });
    }

    @Test
    void shouldSearchProductsByCategory() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/products/search")
                        .queryParam("category", "electronics")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDTO.class)
                .consumeWith(response -> {
                    List<ProductDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertFalse(products.isEmpty());
                    assertFalse(products.stream().noneMatch(p -> categoryRepository.findById(p.getCategoryId()).get().getName().equals("Electronics")));
                });
    }

    @Test
    void shouldFilterProductsByPriceRange() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/products/search")
                        .queryParam("minPrice", 800)
                        .queryParam("maxPrice", 950)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDTO.class)
                .consumeWith(response -> {
                    List<ProductDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertFalse(products.isEmpty());
                    assertFalse(products.stream().noneMatch(p -> p.getPrice().compareTo(BigDecimal.valueOf(800)) >= 0 &&
                            p.getPrice().compareTo(BigDecimal.valueOf(950)) <= 0));
                });
    }
}
