package com.example.cafe.domain.trade.domain.dto.response;

import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long tradeId;
    private TradeStatus status;
    private int totalPrice;
    private String tradeUUID;
}
