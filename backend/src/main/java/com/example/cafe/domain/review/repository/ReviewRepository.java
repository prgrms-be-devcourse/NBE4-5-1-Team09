package com.example.cafe.domain.review.repository;

import com.example.cafe.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    Optional<Review> findByMemberIdAndProductId(Long memberId, Long productId);
    Double findAverageRatingByProductId(Long productId);

    List<Review> findByItem_IdOrderByCreatedAtDesc(Long itemId);

    Double findAverageRatingByItem_Id(Long itemId);
}