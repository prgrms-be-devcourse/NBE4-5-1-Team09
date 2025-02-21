package com.example.cafe.domain.trade.repository;

import com.example.cafe.domain.trade.domain.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByTradeUUID(String tradeUUID);
}
