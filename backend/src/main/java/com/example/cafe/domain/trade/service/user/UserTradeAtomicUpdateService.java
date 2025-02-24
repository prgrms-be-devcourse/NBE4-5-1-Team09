package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.CancelRequestDto;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.response.CancelResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrdersResponseDto;
import com.example.cafe.domain.trade.domain.entity.CartItem;
import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeItem;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import com.example.cafe.domain.trade.portone.service.PortoneService;
import com.example.cafe.domain.trade.repository.TradeRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.cafe.domain.trade.domain.dto.response.OrdersResponseDto.*;
import static com.example.cafe.domain.trade.domain.entity.TradeStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTradeAtomicUpdateService {

    private final TradeRepository tradeRepository;
    private final ItemRepository itemRepository;
    private final PortoneService portoneService;
    private final MemberRepository memberRepository;

    // 단일 상품 주문: 원자적 업데이트 쿼리를 사용하여 재고를 감소하고 주문 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderResponseDto tradeWithItemInfo(Long memberId, OrderRequestItemDto requestItemDto) {
        Member member = getMember(memberId);

        int reqQuantity = requestItemDto.getQuantity();
        if (reqQuantity < 0) {
            throw new RuntimeException("구매 수량은 0보다 작을 수 없습니다");
        }
        // 원자적 업데이트: 재고가 충분할 때만 차감
        int updated = itemRepository.decreaseStock(requestItemDto.getItemId(), reqQuantity);
        if (updated == 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 업데이트 후 최신 item 정보 조회
        Item item = itemRepository.findById(requestItemDto.getItemId())
                .orElseThrow(() -> new RuntimeException("주문 하고자 하는 상품을 찾을 수 없습니다."));

        // Trade 생성
        Trade trade = makeTrade(member, BUY);
        member.getTrades().add(trade);

        TradeItem tradeItem = TradeItem.builder()
                .trade(trade)
                .item(item)
                .quantity(reqQuantity)
                .build();
        tradeItem.setPrice();
        trade.addTradeItem(tradeItem);

        String tradeUUID = generateTradeUUID();
        trade.setTradeUUID(tradeUUID);
        trade.setTotalPrice(reqQuantity * item.getPrice());

        try {
            portoneService.prePurchase(tradeUUID, new BigDecimal(trade.getTotalPrice()));
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException(e);
        }

        tradeRepository.save(trade);

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    // 장바구니 주문: 각 상품에 대해 원자적 업데이트 쿼리를 사용하여 재고 차감을 진행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderResponseDto tradeWithCart(Long memberId) {
        Member member = getMember(memberId);
        List<CartItem> cartItems;
        try {
            cartItems = member.getCart().getCartItems();
        } catch (NullPointerException e) {
            throw new RuntimeException("장바구니 카트가 비어있습니다.");
        }
        if (cartItems.isEmpty()) {
            throw new RuntimeException("장바구니 카트가 비어있습니다.");
        }

        // 각 상품별 재고 차감 처리 (모든 업데이트가 성공해야 주문 진행)
        // 먼저 재고 부족 여부를 미리 체크
        for (CartItem cartItem : cartItems) {
            Item item = itemRepository.findById(cartItem.getItem().getId())
                    .orElseThrow(() -> new RuntimeException("주문하고자 하는 상품을 찾을 수 없습니다."));
            int reqQuantity = cartItem.getQuantity();
            if (reqQuantity < 0) {
                throw new RuntimeException("구매 수량은 0보다 작을 수 없습니다");
            }
            if (item.getItemStatus().equals(ItemStatus.SOLD_OUT) || reqQuantity > item.getStock()) {
                throw new RuntimeException("요청한 상품 중 재고가 부족한 상품이 있습니다.");
            }
        }

        // 모든 상품에 대해 원자적 업데이트 실행
        for (CartItem cartItem : cartItems) {
            int updated = itemRepository.decreaseStock(cartItem.getItem().getId(), cartItem.getQuantity());
            if (updated == 0) {
                throw new RuntimeException("재고 차감 실패: 상품 " + cartItem.getItem().getId() + "의 재고가 부족합니다.");
            }
        }

        // 모든 재고 차감이 성공하면 Trade 생성 및 주문 처리
        Trade trade = makeTrade(member, BUY);
        member.getTrades().add(trade);

        for (CartItem cartItem : cartItems) {
            // 업데이트 후 최신 item 정보 조회
            Item item = itemRepository.findById(cartItem.getItem().getId())
                    .orElseThrow(() -> new RuntimeException("주문하고자 하는 상품을 찾을 수 없습니다."));
            int reqQuantity = cartItem.getQuantity();

            TradeItem tradeItem = TradeItem.builder()
                    .trade(trade)
                    .item(item)
                    .quantity(reqQuantity)
                    .build();
            tradeItem.setPrice();
            trade.addTradeItem(tradeItem);
        }

        trade.setTotalPrice(calculateTotalPrice(trade.getTradeItems()));
        String tradeUUID = generateTradeUUID();
        trade.setTradeUUID(tradeUUID);

        try {
            portoneService.prePurchase(tradeUUID, new BigDecimal(trade.getTotalPrice()));
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException(e);
        }

        // 카트 비우기
        member.getCart().getCartItems().clear();

        tradeRepository.save(trade);

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }


    //사용자 주문 전체 조회
    public OrdersResponseDto showAllTradeItems(Long memberId) {
        Member member = getMember(memberId);
        List<Trade> trades = member.getTrades();
        OrdersResponseDto response = new OrdersResponseDto();
        for (Trade trade : trades) {
            List<TradeItem> tradeItems = trade.getTradeItems();
            for (TradeItem tradeItem : tradeItems) {
                OrderItemsDto itemDto = new OrderItemsDto(tradeItem.getItem().getId(), tradeItem.getQuantity(), tradeItem.getItem().getItemName());
                if (trade.getTradeStatus().equals(BUY)) {
                    response.getBuyList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(PAY)) {
                    response.getPayList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(PREPARE_DELIVERY)) {
                    response.getPrepareDeliveryList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(BEFORE_DELIVERY)) {
                    response.getBeforeDeliveryList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(IN_DELIVERY)) {
                    response.getInDeliveryList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(POST_DELIVERY)) {
                    response.getPostDeliveryList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(REFUSED)) {
                    response.getRefusedList().add(itemDto);
                }
                if (trade.getTradeStatus().equals(REFUND)) {
                    response.getRefundList().add(itemDto);
                }
            }
        }
        return response;
    }

    private Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다"));
        return member;
    }


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

    public CancelResponseDto cancelTrade(CancelRequestDto requestDto) {
        Trade trade = tradeRepository.findByTradeUUID(requestDto.getTradeUUID())
                .orElseThrow(() -> new RuntimeException("해당 거래를 찾을 수 없습니다."));
        if (trade.getTradeStatus().equals(BUY)) {
            return cancelTradeOnBuy(requestDto);
        } else if (trade.getTradeStatus().equals(PAY)) {
            return cancelTradeOnPay(requestDto);
        } else {
            throw new RuntimeException("결제 이후 취소는 관리자에게 문의해주세요.");
        }
    }

    public CancelResponseDto cancelTradeOnBuy(CancelRequestDto requestDto) {
        // 결제 전 취소 로직 (구현 필요)
        return null;
    }

    public CancelResponseDto cancelTradeOnPay(CancelRequestDto requestDto) {
        // 결제 후 취소 로직 (구현 필요)
        return null;
    }

    public void rollBackProductQuantity(Trade trade) {
        trade.getTradeItems().forEach(tradeItem -> {
            Item item = tradeItem.getItem();
            // 롤백 시, 재고를 복구하는 업데이트 쿼리를 별도로 만들 수도 있음
            item.setStock(item.getStock() + tradeItem.getQuantity());
            item.autoCheckQuantityForSetStatus();
        });
    }

    private Trade makeTrade(Member member, TradeStatus status) {
        return Trade.builder()
                .member(member)
                .tradeStatus(status)
                .tradeItems(new ArrayList<>())
                .address(member.getAddress())
                .email(member.getEmail())
                .build();
    }

    public static String generateTradeUUID() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        String currentTime = dateFormat.format(new Date());
        String uuid = java.util.UUID.randomUUID().toString();
        byte[] uuidBytes = uuid.getBytes(StandardCharsets.UTF_8);
        byte[] hashBytes;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hashBytes = md.digest(uuidBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02x", hashBytes[i]));
        }
        return currentTime + sb.toString();
    }

    public int calculateTotalPrice(List<TradeItem> tradeItems) {
        return tradeItems.stream()
                .mapToInt(ti -> ti.getItem().getPrice() * ti.getQuantity())
                .sum();
    }
}