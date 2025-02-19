package com.example.cafe.domain.trade.repository;

import com.example.cafe.domain.trade.domain.entity.TradeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeItemRepository extends JpaRepository<TradeItem, Long> {

}
