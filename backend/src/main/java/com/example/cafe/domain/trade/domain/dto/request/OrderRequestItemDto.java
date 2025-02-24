package com.example.cafe.domain.trade.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderRequestItemDto {
    private Long itemId;
    private int quantity;
}
