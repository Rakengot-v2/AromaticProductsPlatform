package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CategoryEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.*;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderCreationIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID userId;
    private UUID cartId;
    private UUID productId;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        orderItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();

        CategoryEntity category = CategoryEntity.builder()
                .name("Test Category")
                .slug("test-category")
                .build();
        categoryRepository.saveAndFlush(category);

        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userId = userRepository.saveAndFlush(user).getId();

        ProductEntity product = ProductEntity.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .category(category)
                .build();
        productId = productRepository.saveAndFlush(product).getId();

        CartEntity cart = CartEntity.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        cartId = cartRepository.saveAndFlush(cart).getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }


    @Test
    void shouldCreateOrderSuccessfully() {
        assertNotNull(userRepository.findByEmail("test@example.com"), "User not found in DB!");

        webTestClient.post()
                .uri("/api/carts/{cartId}/items", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CartItemDTO(cartId, productId, 2, BigDecimal.valueOf(100)))
                .exchange()
                .expectStatus().isCreated();

        OrderDTO response = webTestClient.post()
                .uri("/api/carts/{cartId}/checkout?address=Kyiv", cartId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(response.getId());
        assertEquals("Kyiv", response.getAddress());
        assertEquals(200, response.getTotal().intValue());

        System.out.println("Checking cart after checkout: " + cartRepository.findById(cartId));

        webTestClient.get()
                .uri("/api/carts/user/{userId}", userId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(0);
    }

    @Test
    void shouldFailCheckoutWithEmptyCart() {
        CartEntity savedCart = cartRepository.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
        assertNotNull(savedCart, "Cart should exist before checkout");
        assertTrue(savedCart.getItems().isEmpty(), "Cart should be empty before checkout");

        webTestClient.post()
                .uri("/api/carts/{cartId}/checkout?address=Kyiv", cartId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cart is empty, cannot proceed to checkout");
    }
}