package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.ReviewDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.ReviewEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.CategoryRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.ReviewRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID productId;
    private UUID userId;

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        reviewRepository.deleteAll();


        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build());

        userId = user.getId();

        CategoryEntity category = categoryRepository.save(CategoryEntity.builder()
                .name("Electronics")
                .slug("electronics")
                .build());

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("iPhone 13")
                .price(BigDecimal.valueOf(999))
                .description("Latest iPhone model")
                .stock(5)
                .imageUrl("https://example.com/iphone13.jpg")
                .isActive(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .build());

        productId = product.getId();

        reviewRepository.save(ReviewEntity.builder()
                .user(user)
                .product(product)
                .rating(5)
                .comment("Great product!")
                .isApproved(true)
                .createdAt(LocalDateTime.now())
                .build());

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldCreateReview() {
        ReviewDTO newReview = new ReviewDTO(userId, productId, 5, "Amazing!", false, LocalDateTime.now());

        webTestClient.post()
                .uri("/api/reviews")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(newReview)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReviewDTO.class)
                .consumeWith(response -> {
                    ReviewDTO review = response.getResponseBody();
                    assertNotNull(review);
                    assertEquals("Amazing!", review.getComment());
                    assertEquals(5, review.getRating());
                    assertFalse(review.getIsApproved());
                });
    }

    @Test
    void shouldGetReviewsByProductId() {
        webTestClient.get()
                .uri("/api/reviews/product/{productId}", productId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewDTO.class)
                .consumeWith(response -> {
                    List<ReviewDTO> reviews = response.getResponseBody();
                    assertNotNull(reviews);
                    assertFalse(reviews.isEmpty());
                });
    }

    @Test
    void shouldFailToCreateReviewForNonexistentProduct() {
        UUID nonexistentProductId = UUID.randomUUID();
        ReviewDTO newReview = new ReviewDTO(userId, nonexistentProductId, 4, "Decent quality", false, LocalDateTime.now());

        webTestClient.post()
                .uri("/api/reviews")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(newReview)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found");
    }
}
