package com.example.cafe.domain.review.dto;

import com.example.cafe.domain.review.entity.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewResponseDto {
    private Long reviewId;         // 리뷰 ID
    private Long memberId;         // 작성자의 멤버 ID
    private Long itemId;           // 상품 ID
    private String reviewContent;  // 리뷰 내용
    private double rating;         // 리뷰 평점
    private LocalDateTime createdAt; // 리뷰 작성 시간
    private LocalDateTime modifiedAt; // 리뷰 수정 시간

    // fromEntity 메서드 추가
    public static ReviewResponseDto fromEntity(Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setReviewId(review.getReview_id());
        dto.setMemberId(review.getMember().getId());
        dto.setItemId(review.getItem().getId());
        dto.setReviewContent(review.getReview_content());
        dto.setRating(review.getRating());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setModifiedAt(review.getModifiedAt());
        return dto;
    }
}
