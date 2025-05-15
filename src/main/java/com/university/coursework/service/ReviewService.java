package com.university.coursework.service;

import com.university.coursework.domain.ReviewDTO;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    List<ReviewDTO> getApprovedReviewsByProductId(UUID productId);
    ReviewDTO createReview(ReviewDTO reviewDTO);
    ReviewDTO approveReview(UUID reviewId);
    void deleteReview(UUID reviewId);
}