package com.example.cafe.domain.trade.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CancelResponseDto {
    private Long tradeId;
    private String tradeUUID;
    private boolean isRefund;
    private int refundPrice;

    private List<RemainingTradeItemDto> tradeItemDtoList;


    //취소된 itemId 와 취소된 수량
    @Data
    @AllArgsConstructor
    public static class RemainingTradeItemDto {
        private Long itemId;
        private Integer quantity;
    }
}
