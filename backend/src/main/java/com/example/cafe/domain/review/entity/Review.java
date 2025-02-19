package com.example.cafe.domain.review.entity;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long review_id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Item item;
    private String review_content;
    private double rating;
    private LocalDateTime created_at;
    private LocalDateTime modified_at;
    
    // 엔티티가 처음 저장될 때 실행
    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
        this.modified_at = LocalDateTime.now(); // 생성 시점과 동일하게 초기화
    }

    // 엔티티가 업데이트될 때 실행
    @PreUpdate
    public void preUpdate() {
        this.modified_at = LocalDateTime.now();
    }
}