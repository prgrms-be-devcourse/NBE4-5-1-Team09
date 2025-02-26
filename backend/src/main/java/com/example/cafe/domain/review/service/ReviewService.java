package com.example.cafe.domain.review.service;

import com.example.cafe.domain.item.service.ItemService;
import com.example.cafe.domain.review.dto.ReviewRequestDto;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.repository.ReviewRepository;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final TradeRepository tradeRepository;
    private final ItemService itemService;

    @Transactional
    public Review createReview(String memberEmail, Long itemId, String reviewContent, double rating) {
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        boolean hasPurchased = tradeRepository.existsByMemberIdAndItemId(member.getId(), itemId);

        // 구매 기록이 없으면 예외 발생
        if (!hasPurchased) {
            throw new IllegalArgumentException("해당 상품을 구매한 사용자만 리뷰를 작성할 수 있습니다.");
        }

        Review review = new Review();
        review.setMember(member);
        review.setItem(item);
        review.setReviewContent(reviewContent);
        review.setRating(rating);

        Review savedReview = reviewRepository.save(review);

        // 평균 평점 다시 계산
        itemService.getAverageRating(itemId);

        return savedReview;
    }

    @Transactional
    public Review updateReview(Long reviewId, String reviewContent, double rating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        review.setReviewContent(reviewContent);
        review.setRating(rating);

        Review updatedReview = reviewRepository.save(review);

        // 평균 평점 다시 계산
        itemService.getAverageRating(review.getItem().getId());

        return updatedReview;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        Long itemId = review.getItem().getId();
        reviewRepository.delete(review);

        // 평균 평점 다시 계산
        itemService.getAverageRating(itemId);
    }


    public List<Review> getReviewsByItem(Long itemId, String memberEmail, ReviewSortType sortType) {
        if (sortType == ReviewSortType.LATEST) {
            return reviewRepository.findByItem_IdAndMember_EmailNotOrderByCreatedAtDesc(itemId, memberEmail);
        } else if (sortType == ReviewSortType.HIGHEST_RATING) {
            return reviewRepository.findByItem_IdAndMember_EmailNotOrderByRatingDesc(itemId, memberEmail);
        } else {
            return reviewRepository.findByItem_IdAndMember_EmailNotOrderByRatingAsc(itemId, memberEmail);
        }
    }

    public List<Review> getReviewsByItemAndMember(Long itemId, String memberEmail) {
        return reviewRepository.findByItemIdAndMemberEmail(itemId, memberEmail);
    }

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

}
