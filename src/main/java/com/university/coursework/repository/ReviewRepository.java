package com.university.coursework.repository;

import com.university.coursework.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    List<ReviewEntity> findByProductId(UUID productId);
    List<ReviewEntity> findByProductIdAndIsApprovedTrue(UUID productId);
    List<ReviewEntity> findByIsApprovedFalse();
}