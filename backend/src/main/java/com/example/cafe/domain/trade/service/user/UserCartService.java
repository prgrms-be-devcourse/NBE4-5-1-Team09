package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.respository.ItemRepository;
import com.example.cafe.domain.trade.domain.dto.ItemAddCartDto;
import com.example.cafe.domain.trade.domain.entity.Cart;
import com.example.cafe.domain.trade.domain.entity.CartItem;
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
    private final ItemRepository itemRepository;


    public void addItemToCart(ItemAddCartDto addItem) {
        /**
         * addItem 으로 해당 유저의 장바구니에 아이템을 추가하는 로직.
         */

        Cart cart = cartRepository.findByMemberId(addItem.getMemberId()).orElseThrow(() -> new RuntimeException("유저 [" + addItem.getMemberId() + "]의 카트를 찾을 수 없습니다."));

        Item item = itemRepository.findById(addItem.getItemId()).orElseThrow(() -> new RuntimeException("해당 아이템을 찾을 수 없어 카트에 추가하지 못하였습니다."));

        CartItem cartItem = cart.getCartItems().stream().filter(cartAddItem -> cartAddItem.getItem().getId().equals(item.getId())).findFirst().orElse(null);

        if (cartItem != null) {
            // 존재한다면 수량 업데이트
            cartItem.setQuantity(cartItem.getQuantity() + addItem.getQuantity());
        } else {
            // 존재하지 않으면 새 CartItem 생성 후 장바구니에 추가
            cartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(addItem.getQuantity())
                    .build();
            cart.getCartItems().add(cartItem);
        }
    }

    
}
