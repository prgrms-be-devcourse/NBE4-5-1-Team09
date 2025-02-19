package com.example.cafe.domain.trade.domain.dto;

import lombok.Data;

@Data
public class ItemCartDto {
    private Long memberId;
    private Long itemId;
    private int quantity;
}
