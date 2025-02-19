package com.example.cafe.domain.member.entity;


import com.example.cafe.domain.trade.domain.entity.Cart;
import com.example.cafe.domain.trade.domain.entity.Trade;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // 이메일
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // 비밀번호
    @Column(name = "password", nullable = false)
    private String password;

    // 주소
    @Column(name = "address", nullable = false)
    private String address;

    // 권한
    @Column(name = "authority", nullable = false)
    private String authority;

    // 관리자 코드 (일반 회원은 null로 처리)
    @Column(name = "admin_code")
    private String adminCode;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trade> trades;
}
