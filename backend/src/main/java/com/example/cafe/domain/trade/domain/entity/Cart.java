package com.example.cafe.domain.trade.domain.entity;

import com.example.cafe.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 장바구니는 한 회원에 속함 (1:1)
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // 장바구니는 여러 CartItem을 가짐 (1:N)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private int totalPrice;

    public void calculateTotalPrice() {
        int tprice = 0;
        for (CartItem cartItem : this.cartItems) {
            tprice += (cartItem.getQuantity() * cartItem.getItem().getPrice());
        }
        this.totalPrice = tprice;
    }
}
