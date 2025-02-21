package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.ItemCartRequestDto;
import com.example.cafe.domain.trade.domain.dto.response.CartListResponseDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.cafe.domain.trade.domain.dto.response.CartListResponseDto.*;
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
    private final MemberRepository memberRepository;

    public CartListResponseDto showCart(Long memberId) {
        Cart cart = getCart(memberId);
        CartListResponseDto response = new CartListResponseDto();
        response.setCartId(cart.getId());
        response.setMemberId(memberId);
        response.setTotalPrice(cart.getTotalPrice());

        for (CartItem cartItem : cart.getCartItems()) {
            CartItemDto cartItemDto = new CartItemDto(cartItem.getItem().getId(), cartItem.getItem().getItemName(), cartItem.getItem().getPrice(), cartItem.getQuantity());
            response.getItems().add(cartItemDto);
        }

        return response;
    }

    public ItemCartResponseDto addItemToCart(ItemCartRequestDto addItem) {
        Cart cart = getCart(addItem.getMemberId());

        Item item = itemRepository.findById(addItem.getItemId()).orElseThrow(() -> new RuntimeException("해당 아이템을 찾을 수 없어 카트에 추가하지 못하였습니다."));
        if (item.getItemStatus().equals(ItemStatus.SOLD_OUT) || item.getStock() == 0) {
            throw new RuntimeException("해당 상품은 품절입니다.");
        }
        item.setStock(item.getStock() - addItem.getQuantity());
        item.autoCheckQuantityForSetStatus();

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

        cart.calculateTotalPrice();

        return getItemCartResponseDto(cart);
    }



    public ItemCartResponseDto editItemToCart(ItemCartRequestDto editItem) {
        Cart cart = getCart(editItem.getMemberId());

        Item item = itemRepository.findById(editItem.getItemId())
                .orElseThrow(() -> new RuntimeException("해당 아이템을 찾을 수 없어 카트에 수정하지 못하였습니다."));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().getId().equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 상품이 카트에 존재하지 않아 수량 수정에 실패하였습니다."));

        int newQuantity = editItem.getQuantity();
        int originalQuantity = cartItem.getQuantity();

        if (newQuantity < 0) {
            throw new RuntimeException("수량은 0 이상이어야 합니다.");
        }

        if (newQuantity == 0) {
            cart.getCartItems().remove(cartItem);
        } else {
            if (newQuantity - cartItem.getQuantity() > item.getStock()) {
                throw new RuntimeException("요청한 수량이 재고를 초과합니다. (재고: " + item.getStock() + ")");
            }
            cartItem.setQuantity(newQuantity);
        }
        item.setStock(item.getStock() - (newQuantity - originalQuantity));

        item.autoCheckQuantityForSetStatus();
        cart.calculateTotalPrice();

        return getItemCartResponseDto(cart);
    }

    public void rollBackItemStock(Long itemId, int rollBackQuantity) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        item.setStock(item.getStock() + rollBackQuantity);
    }


    private Cart getCart(Long memberId) {
        // 회원을 조회합니다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("유저 [" + memberId + "]를 찾을 수 없습니다."));

        // 만약 회원에게 카트가 없다면 새 카트를 생성합니다.
        if (member.getCart() == null) {
            Cart newCart = Cart.builder()
                    .member(member)
                    .cartItems(new ArrayList<>()) // 빈 리스트 초기화
                    .build();
            // 회원과 카트 양쪽에 설정
            member.setCart(newCart);
            cartRepository.save(newCart); // 새 카트를 저장
            return newCart;
        }

        return member.getCart();
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
