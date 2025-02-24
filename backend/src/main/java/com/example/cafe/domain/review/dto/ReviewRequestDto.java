package com.example.cafe.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReviewRequestDto {
    private String memberEmail;    // 작성자의 멤버 이메일
    private Long itemId;           // 리뷰가 작성될 상품 ID
    private String reviewContent;  // 리뷰 내용
    private double rating;         // 리뷰 평점
}
