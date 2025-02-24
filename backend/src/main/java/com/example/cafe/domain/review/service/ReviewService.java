package com.example.cafe.domain.review.service;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.item.service.ItemService;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final ItemService itemService;

    // 리뷰 작성
    @Transactional
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

        Review savedReview = reviewRepository.save(review);
        itemService.getAverageRating(itemId);

        return savedReview;
    }

    // 리뷰 수정
    @Transactional
    public Review updateReview(Long reviewId, String reviewContent, Double rating) {
        if (reviewContent == null || reviewContent.trim().isEmpty() || rating == null) {
            throw new IllegalArgumentException("리뷰 내용과 평점은 필수 항목입니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setReview_content(reviewContent);
        review.setRating(rating);
        review.setModifiedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        itemService.getAverageRating(review.getItem().getId());

        return updatedReview;
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        Long itemId = review.getItem().getId();
        reviewRepository.delete(review);

        itemService.getAverageRating(itemId);
    }

    // 상품별 리뷰 조회 (수동 재시도 3번)
    public List<Review> getReviewsByItem(Long itemId, Long memberId, ReviewSortType sortType) {
        int maxAttempts = 3;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                List<Review> reviews;
                switch (sortType) {
                    case HIGHEST_RATING:
                        reviews = reviewRepository.findByItem_IdAndMember_IdNotOrderByRatingDesc(itemId, memberId);
                        break;
                    case LOWEST_RATING:
                        reviews = reviewRepository.findByItem_IdAndMember_IdNotOrderByRatingAsc(itemId, memberId);
                        break;
                    case LATEST:
                    default:
                        reviews = reviewRepository.findByItem_IdAndMember_IdNotOrderByCreatedAtDesc(itemId, memberId);
                        break;
                }

                if (reviews.isEmpty()) {
                    throw new IllegalArgumentException("아직 리뷰가 없습니다.");
                }

                return reviews;
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new IllegalArgumentException("상품별 리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.", e);
                }
            }
        }
        throw new IllegalArgumentException("상품별 리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
    }

//    // 평균 평점 조회 (수동 재시도 3번)
//    public Double getAverageRating(Long itemId) {
//        int maxAttempts = 3;
//        int attempt = 0;
//        while (attempt < maxAttempts) {
//            try {
//                return reviewRepository.findAverageRatingByItem_Id(itemId);
//            } catch (DataAccessException e) {
//                attempt++;
//                if (attempt >= maxAttempts) {
//                    throw new IllegalArgumentException("평균 평점 조회 중 오류가 발생했습니다. 다시 시도해주세요.", e);
//                }
//            }
//        }
//        throw new IllegalArgumentException("평균 평점 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
//    }

    // 전체 리뷰 조회 (수동 재시도 3번)
    public List<Review> findAllReviews() {
        int maxAttempts = 3;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                return reviewRepository.findAll();
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new IllegalArgumentException("전체 리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.", e);
                }
            }
        }
        throw new IllegalArgumentException("전체 리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
    }

    public List<Review> getReviewsByItemAndMember(Long itemId, Long memberId) {
        int maxAttempts = 3;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                return reviewRepository.findByItemIdAndMemberId(itemId, memberId);
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new IllegalArgumentException("리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.", e);
                }
            }
        }
        throw new IllegalArgumentException("리뷰 조회 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}
