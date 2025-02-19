package com.example.cafe.domain.trade.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * 임시 Member class 입니다. 무영님 엔티티 작업 끝나시면 Trade 에 반영할 예정입니다.
 */
@Entity
public class Member {
    @Id
    @GeneratedValue
    private Long id;
}
