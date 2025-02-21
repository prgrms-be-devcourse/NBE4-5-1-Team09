package com.example.cafe.domain.review.controller;

import com.example.cafe.domain.review.dto.ReviewRequestDto;
import com.example.cafe.domain.review.dto.ReviewResponseDto;
import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.review.entity.ReviewSortType;
import com.example.cafe.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Review API", description = "리뷰 관련 CRUD 및 조회 기능을 제공합니다.")  // Swagger 그룹화 태그
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "회원이 특정 상품에 대해 리뷰를 작성합니다.")
    @PostMapping("/create")
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        Review review = reviewService.createReview(
                reviewRequestDto.getMemberId(),
                reviewRequestDto.getItemId(),
                reviewRequestDto.getReviewContent(),
                reviewRequestDto.getRating()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReviewResponseDto.fromEntity(review));
    }

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰의 내용을 수정합니다.")
    @PutMapping("/update/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @Parameter(description = "수정할 리뷰 ID", example = "1") @PathVariable Long reviewId,
            @RequestBody ReviewRequestDto reviewRequestDto) {

        Review review = reviewService.updateReview(reviewId, reviewRequestDto.getReviewContent(), reviewRequestDto.getRating());

        return ResponseEntity.ok(ReviewResponseDto.fromEntity(review));
    }

    @Operation(summary = "리뷰 삭제", description = "특정 리뷰를 삭제합니다.")
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "삭제할 리뷰 ID", example = "1") @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품별 리뷰 조회", description = "특정 상품에 대한 리뷰 목록을 조회합니다. 내 리뷰는 제외됩니다.")
    @GetMapping("/item/{itemId}/{memberId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByItem(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long itemId,
            @Parameter(description = "회원 ID", example = "123") @PathVariable Long memberId,
            @Parameter(description = "정렬 기준 (LATEST, HIGHEST_RATING, LOWEST_RATING)", example = "LATEST") @RequestParam(defaultValue = "LATEST") ReviewSortType sortType) {

        // 서비스 호출 시 memberId를 전달하여 내 리뷰를 제외한 리뷰를 조회
        List<Review> reviews = reviewService.getReviewsByItem(itemId, memberId, sortType);

        // 리뷰를 ResponseDto로 변환하여 응답
        List<ReviewResponseDto> responseDtos = reviews.stream()
                .map(ReviewResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "평균 평점 조회", description = "특정 상품의 평균 평점을 조회합니다.")
    @GetMapping("/average/{itemId}")
    public ResponseEntity<Double> getAverageRating(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getAverageRating(itemId));
    }

    @Operation(summary = "전체 리뷰 조회 (관리자용)", description = "모든 리뷰를 조회하는 관리자용 API입니다.")
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        List<Review> reviews = reviewService.findAllReviews();
        List<ReviewResponseDto> responseDtos = reviews.stream()
                .map(ReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "내가 작성한 특정 상품 리뷰 조회", description = "현재 로그인한 사용자가 특정 상품에 남긴 리뷰를 조회합니다.")
    @GetMapping("/my/item/{itemId}/{memberId}")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviewsByItem(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long itemId,
            @Parameter(description = "회원 ID", example = "123") @PathVariable Long memberId) {

        List<Review> reviews = reviewService.getReviewsByItemAndMember(itemId, memberId);
        List<ReviewResponseDto> responseDtos = reviews.stream()
                .map(ReviewResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }
}
