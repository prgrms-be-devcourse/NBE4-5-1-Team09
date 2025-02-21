package com.example.cafe.domain.trade.domain.dto.request;

import lombok.Data;

@Data
public class ItemCartRequestDto {
    private Long memberId;
    private Long itemId;
    private int quantity;
}
