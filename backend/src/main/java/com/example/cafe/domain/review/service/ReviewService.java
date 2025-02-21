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
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.DataAccessException;

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
    public Review createReview(Long memberId, Long itemId, String reviewContent, Double rating) {
        if (reviewContent == null || reviewContent.trim().isEmpty() || rating == null) {
            throw new IllegalArgumentException("리뷰 내용과 평점은 필수 항목입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

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
    public Review updateReview(Long reviewId, String reviewContent, Double rating) {
        if (reviewContent == null || reviewContent.trim().isEmpty() || rating == null) {
            throw new IllegalArgumentException("리뷰 내용과 평점은 필수 항목입니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setReview_content(reviewContent);
        review.setRating(rating);
        review.setModifiedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        reviewRepository.delete(review);
    }

    // 상품별 리뷰 조회
    @Retryable(value = DataAccessException.class, maxAttempts = 3)
    public List<Review> getReviewsByItem(Long itemId, ReviewSortType sortType) {
        try {
            List<Review> reviews;
            switch (sortType) {
                case HIGHEST_RATING:
                    reviews = reviewRepository.findByItem_IdOrderByRatingDesc(itemId);
                    break;
                case LOWEST_RATING:
                    reviews = reviewRepository.findByItem_IdOrderByRatingAsc(itemId);
                    break;
                case LATEST:
                default:
                    reviews = reviewRepository.findByItem_IdOrderByCreatedAtDesc(itemId);
                    break;
            }

            if (reviews.isEmpty()) {
                throw new IllegalArgumentException("아직 리뷰가 없습니다.");
            }

            return reviews;
        } catch (DataAccessException e) {
            throw new IllegalArgumentException("상품별 리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

    // 평균 평점 조회
    @Retryable(value = DataAccessException.class, maxAttempts = 3)
    public Double getAverageRating(Long itemId) {
        try {
            return reviewRepository.findAverageRatingByItem_Id(itemId);
        } catch (DataAccessException e) {
            throw new IllegalArgumentException("평균 평점 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

    // 전체 리뷰 조회 (관리자용)
    @Retryable(value = DataAccessException.class, maxAttempts = 3)
    public List<Review> findAllReviews() {
        try {
            return reviewRepository.findAll();
        } catch (DataAccessException e) {
            throw new IllegalArgumentException("전체 리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
}
