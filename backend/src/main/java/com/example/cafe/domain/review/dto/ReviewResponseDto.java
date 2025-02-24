package com.example.cafe.domain.review.dto;

import com.example.cafe.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long reviewId;
    private String memberEmail;  // 이메일로 변경
    private Long itemId;
    private String reviewContent;
    private double rating;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ReviewResponseDto fromEntity(Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setReviewId(review.getId());
        dto.setMemberEmail(review.getMember().getEmail());  // 이메일로 변경
        dto.setItemId(review.getItem().getId());
        dto.setReviewContent(review.getReviewContent());
        dto.setRating(review.getRating());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setModifiedAt(review.getModifiedAt());
        return dto;
    }
}
