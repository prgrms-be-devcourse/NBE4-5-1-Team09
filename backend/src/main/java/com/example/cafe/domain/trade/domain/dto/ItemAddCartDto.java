package com.example.cafe.domain.trade.domain.dto;

import lombok.Data;

@Data
public class ItemAddCartDto {
    private Long memberId;
    private Long itemId;
    private int quantity;
}
