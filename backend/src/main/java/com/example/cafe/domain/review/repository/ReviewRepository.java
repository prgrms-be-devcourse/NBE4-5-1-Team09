package com.example.cafe.domain.review.repository;

import com.example.cafe.domain.review.entity.Review;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItem_IdOrderByCreatedAtDesc(Long itemId); // 최신순
    List<Review> findByItem_IdOrderByRatingDesc(Long itemId);    // 평점 높은 순
    List<Review> findByItem_IdOrderByRatingAsc(Long itemId);     // 평점 낮은 순
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
    Double findAverageRatingByItem_Id(Long itemId);
}