package com.example.cafe.domain.trade.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CancelRequestDto {
    private String tradeUUID;
    private List<CancelItemRequest> cancelItemList;

    @Data
    @AllArgsConstructor
    public static class CancelItemRequest {
        private Long itemId;
        private Integer quantity;
    }
}
