public void processPayment(String uuid, int payAmount) {
  Trade trade = tradeRepository.findByTradeUUID(uuid)
          .orElseThrow(() -> new RuntimeException("해당 거래를 찾을 수 없습니다: " + uuid));

  try {
      if (trade.getTradeStatus() == PAY) {
          throw new RuntimeException("이미 결제가 완료된 거래 입니다.");
      } else if (trade.getTradeStatus() == TradeStatus.REFUSED) {
          throw new RuntimeException("주문이 이미 취소된 거래 입니다.");
      }
      if (!trade.getTotalPrice().equals(payAmount)) {
          throw new RuntimeException("주문 금액과 결제 금액이 다릅니다.");
      }
  } catch (Exception e) {
      trade.setTradeStatus(TradeStatus.REFUSED);
      rollBackProductQuantity(trade);
      throw e;
  }
  trade.setTradeStatus(PAY);
}
