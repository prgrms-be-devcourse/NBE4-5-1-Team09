package com.example.cafe.domain.trade.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CartListResponseDto {
    private Long cartId;
    private Long memberId;
    private int totalPrice;

    private List<CartItemDto> items;

    public CartListResponseDto() {
        this.items = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class CartItemDto {
        private Long itemId;
        private String itemName;
        private int price;
        private int quantity;
    }
}
