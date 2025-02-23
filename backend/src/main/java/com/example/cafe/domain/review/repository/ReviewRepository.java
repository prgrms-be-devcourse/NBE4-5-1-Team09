package com.example.cafe.domain.review.repository;

import com.example.cafe.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItem_IdAndMember_IdNotOrderByCreatedAtDesc(Long itemId, Long memberId); // 최신순, 내 리뷰 제외
    List<Review> findByItem_IdAndMember_IdNotOrderByRatingDesc(Long itemId, Long memberId);    // 평점 높은 순, 내 리뷰 제외
    List<Review> findByItem_IdAndMember_IdNotOrderByRatingAsc(Long itemId, Long memberId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
    Double findAverageRatingByItem_Id(Long itemId);
    @Query("SELECT r FROM Review r WHERE r.item.id = :itemId AND r.member.id = :memberId")
    List<Review> findByItemIdAndMemberId(Long itemId, Long memberId);
}