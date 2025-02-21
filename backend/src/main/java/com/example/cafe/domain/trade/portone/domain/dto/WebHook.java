package com.example.cafe.domain.trade.portone.domain.dto;

import lombok.Data;

@Data
public class WebHook {
    private String imp_uid; // 결제 번호
    private String merchant_uid; // 주문 번호
    private String status; // 결제 결과
    private String cancellation_id;
}
