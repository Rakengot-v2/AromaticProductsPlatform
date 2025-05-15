package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.OrderDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.OrderEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.OrderRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderHistoryIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;
    private UUID orderId;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build());

        userId = user.getId();

        OrderEntity order = orderRepository.save(OrderEntity.builder()
                .user(user)
                .total(BigDecimal.valueOf(200))
                .status("COMPLETED")
                .address("Kyiv")
                .createdAt(LocalDateTime.now())
                .items(List.of())
                .build());

        orderId = order.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldRetrieveOrderHistoryForUser() {
        webTestClient.get()
                .uri("/api/orders/user/{userId}", userId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(orderId.toString())
                .jsonPath("$[0].total").isEqualTo(200)
                .jsonPath("$[0].status").isEqualTo("COMPLETED")
                .jsonPath("$[0].address").isEqualTo("Kyiv")
                .jsonPath("$[0].createdAt").exists();
    }

    @Test
    void shouldReturnEmptyOrderHistoryForUserWithoutOrders() {
        UserEntity newUser = userRepository.save(UserEntity.builder()
                .email("newuser@example.com")
                .password("password")
                .username("newuser")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build());
        UUID newUserId = newUser.getId();

        webTestClient.get()
                .uri("/api/orders/user/{userId}", newUserId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldRetrieveOrderDetails() {
        webTestClient.get()
                .uri("/api/orders/user/{userId}", userId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderDTO.class)
                .consumeWith(response -> {
                    List<OrderDTO> orders = response.getResponseBody();
                    assertNotNull(orders);
                    assertFalse(orders.isEmpty(), "Order history should not be empty");
                    assertEquals(0, orders.get(0).getTotal().compareTo(BigDecimal.valueOf(200)));                });
    }
}
