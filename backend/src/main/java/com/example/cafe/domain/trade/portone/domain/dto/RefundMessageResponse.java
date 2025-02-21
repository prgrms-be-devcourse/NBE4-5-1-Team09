package com.example.cafe.domain.trade.portone.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundMessageResponse {
    private Long refundTradeId;
    private int refundAmount;
    private String refundReason;
}
