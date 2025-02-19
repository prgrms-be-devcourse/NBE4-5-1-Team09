package com.example.cafe.domain.trade.domain.entity;

import com.example.cafe.domain.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "cart_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 각 CartItem은 하나의 Cart에 속함 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // 각 CartItem은 하나의 Product를 참조 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Item item;

    // 추가: 해당 상품의 수량 등 정보를 담을 수 있음
    private int quantity;
}