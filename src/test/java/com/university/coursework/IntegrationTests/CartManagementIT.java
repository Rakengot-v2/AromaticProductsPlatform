package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.CartRepository;
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


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID cartId;
    private UUID productId;

    @BeforeEach
    void setup() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build());

        CategoryEntity category = categoryRepository.save(CategoryEntity.builder()
                .name("Electronics")
                .slug("electronics")
                .build());

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .isActive(true)
                .category(category)
                .createdAt(LocalDateTime.now())
                .build());

        productId = product.getId();

        CartEntity cart = cartRepository.save(CartEntity.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build());

        cartId = cart.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldAddProductToCart() {
        webTestClient.post()
                .uri("/api/carts/{cartId}/items", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(new CartItemDTO(cartId, productId, 2, BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].productId").isEqualTo(productId.toString())
                .jsonPath("$.items[0].quantity").isEqualTo(2);
    }

    @Test
    void shouldUpdateProductQuantityInCart() {
        webTestClient.post()
                .uri("/api/carts/{cartId}/items", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(new CartItemDTO(cartId, productId, 2, BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/carts/{cartId}/items", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(new CartItemDTO(cartId, productId, 5, BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].quantity").isEqualTo(7);
    }

    @Test
    void shouldFailToAddProductToNonexistentCart() {
        UUID nonexistentCartId = UUID.randomUUID();

        webTestClient.post()
                .uri("/api/carts/{cartId}/items", nonexistentCartId)
                .header("Authorization", "Bearer " + userToken).
                contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(new CartItemDTO(nonexistentCartId, productId, 2, BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cart not found with id: " + nonexistentCartId);
    }

    @Test
    void shouldFailToAddNonexistentProductToCart() {
        UUID nonexistentProductId = UUID.randomUUID();

        webTestClient.post()
                .uri("/api/carts/{cartId}/items", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(new CartItemDTO(cartId, nonexistentProductId, 2, BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }
}

