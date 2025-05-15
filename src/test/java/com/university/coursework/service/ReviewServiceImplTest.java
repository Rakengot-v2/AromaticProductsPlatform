package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.ReviewDTO;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.ReviewEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.ReviewRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    private ReviewServiceImpl reviewService;

    private UUID reviewId;
    private UUID productId;
    private UUID userId;
    private ReviewEntity reviewEntity;
    private ReviewDTO reviewDTO;
    private ProductEntity productEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, productRepository, userRepository);
        reviewId = UUID.randomUUID();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();

        productEntity = new ProductEntity();
        productEntity.setId(productId);
        productEntity.setName("Smartphone");

        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("testuser");


        reviewEntity = new ReviewEntity();
        reviewEntity.setId(reviewId);
        reviewEntity.setProduct(productEntity);
        reviewEntity.setUser(userEntity);
        reviewEntity.setRating(5);
        reviewEntity.setComment("Great product!");
        reviewEntity.setIsApproved(false);

        reviewDTO = ReviewDTO.builder()
                .userId(userId)
                .productId(productId)
                .rating(5)
                .comment("Great product!")
                .isApproved(false)
                .build();
    }

    @Test
    void testGetApprovedReviewsByProductId() {
        when(reviewRepository.findByProductIdAndIsApprovedTrue(productId)).thenReturn(List.of(reviewEntity));

        List<ReviewDTO> result = reviewService.getApprovedReviewsByProductId(productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByProductIdAndIsApprovedTrue(productId);
    }

    @Test
    void testCreateReview() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);

        ReviewDTO result = reviewService.createReview(reviewDTO);

        assertNotNull(result);
        assertEquals("Great product!", result.getComment());
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    void testApproveReview() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);

        ReviewDTO result = reviewService.approveReview(reviewId);

        assertNotNull(result);
        assertTrue(result.getIsApproved());
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    void testDeleteReview() {
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository).deleteById(reviewId);
    }

    @Test
    void testDeleteReviewNotFound() {
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> reviewService.deleteReview(reviewId));
    }
}
