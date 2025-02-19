package com.example.cafe.domain.review.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponseDto {
    private Long reviewId;         // 리뷰 ID
    private Long memberId;         // 작성자의 멤버 ID
    private Long itemId;           // 상품 ID
    private String reviewContent;  // 리뷰 내용
    private double rating;         // 리뷰 평점
    private LocalDateTime createdAt; // 리뷰 작성 시간
    private LocalDateTime modifiedAt; // 리뷰 수정 시간
}
