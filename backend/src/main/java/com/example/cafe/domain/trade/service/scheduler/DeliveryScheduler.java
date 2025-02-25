package com.example.cafe.domain.trade.service.scheduler;

import ch.qos.logback.classic.Logger;
import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeliveryScheduler {

    private final TradeRepository tradeRepository;

    @Scheduled(cron = "0 */10 * * * *")
    public void updateStatusToBeforeDelivery() {

        List<Trade> prepareDeliveryTrades = tradeRepository.findByTradeStatus(TradeStatus.PREPARE_DELIVERY);

        for (Trade prepareDeliveryTrade : prepareDeliveryTrades) {
            prepareDeliveryTrade.setTradeStatus(TradeStatus.BEFORE_DELIVERY);
        }
        log.info("trade 중 status 가 Prepare Delivery 인 거래를 Before Delivery 로 업데이트를 완료했습니다.");
    }
}
