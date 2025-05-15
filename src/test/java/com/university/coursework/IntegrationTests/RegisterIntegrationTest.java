package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.RegisterRequest;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.CartRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RegisterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void setup() {
        cartRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newUser");
        request.setPassword("securepassword");
        request.setPhone("1234567890");

        webTestClient.post()
                .uri("/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Registration successful");
    }

    @Test
    void shouldFailToRegisterUserWithExistingEmail() {
        userRepository.save(UserEntity.builder()
                .email("existing@example.com")
                .username("existingUser")
                .password("password")
                .createdAt(LocalDateTime.now())
                .role(Role.USER)
                .build());

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("newUser");
        request.setPassword("securepassword");
        request.setPhone("1234567890");

        webTestClient.post()
                .uri("/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User with email existing@example.com already exists");
    }

    @Test
    void shouldFailToRegisterWithoutEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("securepassword");
        request.setPhone("1234567890");

        webTestClient.post()
                .uri("/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email must be provided");
    }
}
