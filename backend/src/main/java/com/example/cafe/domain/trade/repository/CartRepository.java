package com.example.cafe.domain.trade.repository;

import com.example.cafe.domain.trade.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberId(Long memberId);
public interface CartRepository extends JpaRepository<Cart, Long> {
}
