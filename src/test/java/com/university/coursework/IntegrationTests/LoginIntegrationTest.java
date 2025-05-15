package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.LoginRequest;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.CartRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private String passwordHash;
    private UUID userId;

    @BeforeEach
    void setup() {
        cartRepository.deleteAll();
        userRepository.deleteAll();

        passwordHash = encoder.encode("securepassword");

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("testuser@example.com")
                .username("testuser")
                .password(passwordHash)
                .phone("1234567890")
                .createdAt(LocalDateTime.now())
                .role(Role.USER)
                .build());

        userId = user.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("securepassword");

        webTestClient.post()
                .uri("/login")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Login successful")
                .jsonPath("$.user.email").isEqualTo("testuser@example.com");
    }

    @Test
    void shouldFailLoginWithIncorrectPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("wrongpassword");

        webTestClient.post()
                .uri("/login")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid email or password");
    }

    @Test
    void shouldFailLoginWithNonexistentEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("fakeuser@example.com");
        request.setPassword("securepassword");

        webTestClient.post()
                .uri("/login")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid email or password");
    }
}
