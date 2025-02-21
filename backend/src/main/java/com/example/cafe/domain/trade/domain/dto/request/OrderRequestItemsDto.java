package com.example.cafe.domain.trade.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderRequestItemsDto {
    private List<OrderRequestItemDto> itemDtoList;
}
