package com.example.cafe.domain.trade.domain.entity;

public enum TradeStatus {
    WANT("장바구니"), //(장바구니 담겨있는 상태)
    BUY("구매요청"), //(구매하기 요청 후 결제 완료 이전 상태)
    PAY("결제완료"), //(결제 완료 이후 상태)
    BEFORE_DELIVERY("배송대기"), // (배송 대기)
    PREPARE_DELIVERY("배송준비"), // (배송 준비)
    IN_DELIVERY("배송중"), // (배송 중)
    POST_DELIVERY("배송완료"), // (배송 완료)
    REFUSED("주문취소"),
    REFUND("환불 완료"); // (주문 취소) → 어떤 상태에서든 취소 되면, REFUSED 로 통일.
    REFUSED("주문취소"); // (주문 취소) → 어떤 상태에서든 취소 되면, REFUSED 로 통일.

    private final String status;

    public String getStatus() {
        return status;
    }

    private TradeStatus(String status) {
        this.status = status;
    }
}
