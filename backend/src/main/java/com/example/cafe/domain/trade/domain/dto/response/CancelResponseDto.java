package com.example.cafe.domain.trade.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CancelResponseDto {
    private Long memberId;
    private Long tradeId;
    private String tradeUUID;
    private boolean isRefund;
    private int refundPrice;

    private List<TradeItemDto> tradeItemDtoList;

    @Data
    @AllArgsConstructor
    public static class TradeItemDto {
        private Long itemId;
        private Integer quantity;
    }
}
