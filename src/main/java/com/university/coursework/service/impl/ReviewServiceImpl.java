package com.university.coursework.service.impl;

import com.university.coursework.domain.ReviewDTO;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.ReviewEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.ReviewRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<ReviewDTO> getApprovedReviewsByProductId(UUID productId) {
        return reviewRepository.findByProductIdAndIsApprovedTrue(productId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        ProductEntity product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        UserEntity user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ProductNotFoundException("User not found"));

        ReviewEntity review = mapToEntity(reviewDTO);
        review.setProduct(product);
        review.setUser(user);
        review.setIsApproved(false); // Reviews require moderation by default

        ReviewEntity savedReview = reviewRepository.save(review);
        return mapToDto(savedReview);
    }

    @Override
    public ReviewDTO approveReview(UUID reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ProductNotFoundException("Review not found"));

        review.setIsApproved(true);
        ReviewEntity approvedReview = reviewRepository.save(review);
        return mapToDto(approvedReview);
    }

    @Override
    public void deleteReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ProductNotFoundException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
    }

    private ReviewDTO mapToDto(ReviewEntity entity) {
        return ReviewDTO.builder()
                .userId(entity.getUser().getId())
                .productId(entity.getProduct().getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .isApproved(entity.getIsApproved())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ReviewEntity mapToEntity(ReviewDTO dto) {
        return ReviewEntity.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .isApproved(dto.getIsApproved())
                .build();
    }
}