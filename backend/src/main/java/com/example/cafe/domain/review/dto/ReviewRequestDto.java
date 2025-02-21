package com.example.cafe.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReviewRequestDto {
    private Long memberId;         // 작성자의 멤버 ID
    private Long itemId;           // 리뷰가 작성될 상품 ID
    private String reviewContent;  // 리뷰 내용
    private double rating;         // 리뷰 평점
}
