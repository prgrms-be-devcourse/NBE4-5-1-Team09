package com.example.cafe.domain.trade.domain.dto;

import com.example.cafe.domain.item.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemCartResponseDto {
    private Long cartId;
    private List<ItemCartItemInfo> cartItemInfoList;
    private int totalPrice;

    @Data
    @AllArgsConstructor
    public static class ItemCartItemInfo{
        private Long itemId;
        private String itemName;
        private int price;
        private ItemStatus itemStatus;
        private int quantity;
    }
}
