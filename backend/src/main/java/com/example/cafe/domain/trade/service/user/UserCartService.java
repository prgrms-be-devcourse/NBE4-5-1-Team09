package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.trade.domain.dto.ItemAddCartDto;
import com.example.cafe.domain.trade.repository.CartItemRepository;
import com.example.cafe.domain.trade.repository.CartRepository;
import com.example.cafe.domain.trade.repository.TradeItemRepository;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCartService {
    private final TradeRepository tradeRepository;
    private final TradeItemRepository tradeItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    public void addItemToCart(ItemAddCartDto addItem) {
        /**
         * addItem 으로 해당 유저의 장바구니에 아이템을 추가하는 로직.
         */
    }
}
