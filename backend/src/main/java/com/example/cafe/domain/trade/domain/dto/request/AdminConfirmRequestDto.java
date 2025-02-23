package com.example.cafe.domain.trade.domain.dto.request;

import lombok.Data;

@Data
public class AdminConfirmRequestDto {
    private String tradeUUID;
    private boolean changeToDeliveryReady;
}
