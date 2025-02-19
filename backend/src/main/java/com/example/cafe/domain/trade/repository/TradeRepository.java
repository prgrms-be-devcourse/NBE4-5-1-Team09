package com.example.cafe.domain.trade.repository;

import com.example.cafe.domain.trade.domain.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
