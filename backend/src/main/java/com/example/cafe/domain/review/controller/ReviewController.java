package com.example.cafe.domain.review.controller;

import com.example.cafe.domain.review.dto.ReviewRequestDto;
import com.example.cafe.domain.review.dto.ReviewResponseDto;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor  // 🔹 생성자 주입 (Lombok 활용)
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/create")
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        Review review = reviewService.createReview(
                reviewRequestDto.getMemberId(),
                reviewRequestDto.getItemId(),
                reviewRequestDto.getReviewContent(),
                reviewRequestDto.getRating()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReviewResponseDto.fromEntity(review));  // fromEntity 메서드 사용
    }

    // 리뷰 수정
    @PutMapping("/update/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDto reviewRequestDto) {

        Review review = reviewService.updateReview(reviewId, reviewRequestDto.getReviewContent(), reviewRequestDto.getRating());

        return ResponseEntity.ok(ReviewResponseDto.fromEntity(review));  // fromEntity 메서드 사용
    }

    // 리뷰 삭제
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    // 상품별 리뷰 조회 (정렬 기능 추가)
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByItem(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sortType) {

        List<Review> reviews = reviewService.getReviewsByItem(itemId, sortType);

        List<ReviewResponseDto> responseDtos = reviews.stream()
                .map(ReviewResponseDto::fromEntity)  // fromEntity 메서드 사용
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    // 평균 평점 조회
    @GetMapping("/average/{itemId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getAverageRating(itemId));
    }

    // 관리자용 전체 리뷰 조회
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        List<Review> reviews = reviewService.findAllReviews(); // Review 엔티티 리스트 반환
        List<ReviewResponseDto> responseDtos = reviews.stream()
                .map(ReviewResponseDto::fromEntity)  // fromEntity 메서드 사용
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
}
