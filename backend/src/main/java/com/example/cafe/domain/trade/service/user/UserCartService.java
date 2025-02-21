package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.trade.domain.dto.request.ItemCartRequestDto;
import com.example.cafe.domain.trade.domain.dto.response.ItemCartResponseDto;
import com.example.cafe.domain.trade.domain.entity.Cart;
import com.example.cafe.domain.trade.domain.entity.CartItem;
import com.example.cafe.domain.trade.repository.CartItemRepository;
import com.example.cafe.domain.trade.repository.CartRepository;
import com.example.cafe.domain.trade.repository.TradeItemRepository;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.cafe.domain.trade.domain.dto.response.ItemCartResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCartService {
    private final TradeRepository tradeRepository;
    private final TradeItemRepository tradeItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public ItemCartResponseDto addItemToCart(ItemCartRequestDto addItem) {
        Cart cart = getCart(addItem);

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

        return getItemCartResponseDto(cart);
    }



    public ItemCartResponseDto QuantityItemToCart(ItemCartRequestDto editItem) {
        Cart cart = getCart(editItem);

        Item item = itemRepository.findById(editItem.getItemId()).orElseThrow(() -> new RuntimeException("해당 아이템을 찾을 수 없어 카트에 추가하지 못하였습니다."));

        CartItem cartItem = cart.getCartItems().stream().filter(cartAddItem -> cartAddItem.getItem().getId().equals(item.getId())).findFirst().orElse(null);

        if (cartItem == null) {
            throw new RuntimeException("해당 상품이 카트에 존재하지 않아 수량을 수정에 실패하였습니다.");
        }

        if (cartItem.getQuantity() < editItem.getQuantity()) {
            throw new RuntimeException("장바구니에 있는 수량보다 취소 수량이 더 많습니다.");
        }
        //삭제 요청 수량이 장바구니 수량과 동일하거나, 상품 삭제 요청. 수량 감소 요청 숫자가 0이면 삭제요청으로 간주.
        else if (cartItem.getQuantity() == editItem.getQuantity() || editItem.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
        }
        else {
            cartItem.setQuantity(cartItem.getQuantity() - editItem.getQuantity());
        }

        return getItemCartResponseDto(cart);
    }



    private Cart getCart(ItemCartRequestDto itemCartDto) {
        return cartRepository.findByMemberId(itemCartDto.getMemberId()).orElseThrow(() -> new RuntimeException("유저 [" + itemCartDto.getMemberId() + "]의 카트를 찾을 수 없습니다."));
    }

    private ItemCartResponseDto getItemCartResponseDto(Cart cart) {
        List<ItemCartItemInfo> cartItemInfos = cart.getCartItems().stream().map(ci -> new ItemCartItemInfo(
                ci.getItem().getId(),
                ci.getItem().getItemName(),
                ci.getItem().getPrice(),
                ci.getItem().getItemStatus(),
                ci.getQuantity()
        )).collect(Collectors.toList());

        int totalPrice = cart.getCartItems().stream().mapToInt(ci -> ci.getItem().getPrice() * ci.getQuantity()).sum();

        return new ItemCartResponseDto(cart.getId(), cartItemInfos, totalPrice);
    }
}
