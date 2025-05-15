package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.UserDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID userId;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .phone("1234567890")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build());

        userId = user.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldUpdateUserProfile() {
        UserDTO updatedUser = new UserDTO("updated@example.com", "newpassword", "updatedUser", "1234567890");

        webTestClient.put()
                .uri("/api/users/{id}", userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .consumeWith(response -> {
                    UserDTO user = response.getResponseBody();
                    assertNotNull(user);
                    assertEquals("updated@example.com", user.getEmail());
                    assertEquals("updatedUser", user.getUsername());
                });
    }

    @Test
    void shouldConfirmUserProfileUpdate() {
        UserDTO updatedUser = new UserDTO("new@example.com", "newUser", "newpassword", "1234567890");

        webTestClient.put()
                .uri("/api/users/{id}", userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .consumeWith(response -> {
                    UserDTO user = response.getResponseBody();
                    assertNotNull(user);
                    assertEquals(updatedUser.getEmail(), user.getEmail());
                    assertEquals(updatedUser.getUsername(), user.getUsername());
                });
    }

    @Test
    void shouldFailToUpdateNonexistentUser() {
        UUID nonexistentUserId = UUID.randomUUID();

        UserDTO updatedUser = new UserDTO("fake@example.com", "fakeUser", "fakepassword", "1234567890");

        webTestClient.put()
                .uri("/api/users/{id}", nonexistentUserId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User not found with id: " + nonexistentUserId);
    }

    @Test
    void shouldFailToUpdateUserWithInvalidEmail() {
        UserDTO invalidUser = new UserDTO(null, "testuser", "password", "1234567890");

        webTestClient.put()
                .uri("/api/users/{id}", userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(invalidUser)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email must be provided");
    }
}
