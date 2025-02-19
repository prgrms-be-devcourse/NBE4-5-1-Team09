package com.example.cafe.domain.trade.service.admin;

import com.example.cafe.domain.trade.repository.TradeItemRepository;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminTradeService {

    private final TradeRepository tradeRepository;
    private final TradeItemRepository tradeItemRepository;
}

