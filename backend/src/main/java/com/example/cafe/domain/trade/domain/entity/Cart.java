package com.example.cafe.domain.trade.domain.entity;

import com.example.cafe.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 장바구니는 한 회원에 속함 (1:1)
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;


}
