package com.example.cafe.domain.review.repository;

import com.example.cafe.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByItem_IdAndMember_EmailNotOrderByCreatedAtDesc(Long itemId, String memberEmail);
    List<Review> findByItem_IdAndMember_EmailNotOrderByRatingDesc(Long itemId, String memberEmail);
    List<Review> findByItem_IdAndMember_EmailNotOrderByRatingAsc(Long itemId, String memberEmail);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
    Double findAverageRatingByItemId(Long itemId);

    @Query("SELECT r FROM Review r WHERE r.item.id = :itemId AND r.member.email = :memberEmail")
    List<Review> findByItemIdAndMemberEmail(Long itemId, String memberEmail);

    Optional<Review> findByMemberEmail(String email);  // 이메일로 리뷰 찾기
}
