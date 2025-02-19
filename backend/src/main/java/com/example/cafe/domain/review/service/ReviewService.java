package com.example.cafe.domain.review.service;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.respository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    // 리뷰 작성
    public Review createReview(Long memberId, Long itemId, String reviewContent, double rating) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Review review = new Review();
        review.setMember(member);
        review.setItem(item);
        review.setReview_content(reviewContent);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setModifiedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // 리뷰 수정
    public Review updateReview(Long reviewId, String reviewContent, double rating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setReview_content(reviewContent);
        review.setRating(rating);
        review.setModifiedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        reviewRepository.delete(review);
    }

    // 상품별 리뷰 조회
    public List<Review> getReviewsByItem(Long itemId, ReviewSortType sortType) {
        switch (sortType) {
            case HIGHEST_RATING:
                return reviewRepository.findByItem_IdOrderByRatingDesc(itemId);
            case LOWEST_RATING:
                return reviewRepository.findByItem_IdOrderByRatingAsc(itemId);
            case LATEST:
            default:
                return reviewRepository.findByItem_IdOrderByCreatedAtDesc(itemId);
        }
    }

    // 평균 평점 조회
    public Double getAverageRating(Long itemId) {
        return reviewRepository.findAverageRatingByItem_Id(itemId);
    }

    // 전체 리뷰 조회 (관리자용)
    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }
}
