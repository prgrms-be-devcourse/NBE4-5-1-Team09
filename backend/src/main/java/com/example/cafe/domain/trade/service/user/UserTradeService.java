package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.domain.entity.CartItem;
import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeItem;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import com.example.cafe.domain.trade.portone.service.PortoneService;
import com.example.cafe.domain.trade.repository.CartItemRepository;
import com.example.cafe.domain.trade.repository.CartRepository;
import com.example.cafe.domain.trade.repository.TradeItemRepository;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.cafe.domain.trade.domain.entity.TradeStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTradeService {

    private final TradeRepository tradeRepository;
    private final TradeItemRepository tradeItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final PortoneService portoneService;
    private final MemberRepository memberRepository;

    /**
     * 1. 장바구니에서 주문 요청
     * 2. 상품 페이지에서 바로 주문 요청
     * 3. 결제 요청
     */

    public OrderResponseDto tradeWithCart(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다"));
        List<CartItem> cartItems = member.getCart().getCartItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("장바구니 카드가 비어있습니다.");
        }

        for (CartItem cartItem : cartItems) {
            if (!stockValidCheck(new OrderRequestItemDto(member.getId(),cartItem.getItem().getId(), cartItem.getQuantity()))) {
                throw new RuntimeException("요청한 상품 중 재고가 부족한 상품이 있습니다.");
            }
        }

        Trade trade = Trade.builder()
                .member(member)
                .tradeStatus(BUY) // 거래 상태 초기화
                .tradeItems(new ArrayList<>()) // 리스트 초기화
                .build();

        member.getTrades().add(trade);

        for (CartItem cartItem : cartItems) {
             Item item = itemRepository.findById(cartItem.getItem().getId()).orElseThrow(() -> new RuntimeException("주문하고자 하는 상품을 찾을 수 없습니다."));

            item.setStock(item.getStock() - cartItem.getQuantity());
            item.autoCheckQuantityForSetStatus();

            TradeItem tradeItem = TradeItem.builder()
                    .trade(trade)
                    .item(item)
                    .quantity(cartItem.getQuantity())
                    .build();

            tradeItem.setPrice();

            trade.addTradeItem(tradeItem);
        }

        trade.setTotalPrice(calculateTotalPrice(trade.getTradeItems()));

        String tradeUUID = generateTradeUUID();
        trade.setTradeUUID(tradeUUID);

        try {
            portoneService.prePurchase(tradeUUID,new BigDecimal(trade.getTotalPrice()));
        } catch (Exception e) {
            throw new RuntimeException(e + " : 구매 대행사 오류로 인해 결제 요청에 실패하였습니다.");
        }

        // 카트 비우기
        member.getCart().getCartItems().clear();

        //Trade 저장 (cascade 설정이 되어있다면 TradeItem도 함께 저장됨)
        tradeRepository.save(trade);

        //응답 객체 생성 및 반환
        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    public OrderResponseDto tradeWithItemInfo(OrderRequestItemDto requestItemDto) {
        Member member = memberRepository.findById(requestItemDto.getMemberId()).orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다"));
        if (!stockValidCheck(requestItemDto)) {
            throw new RuntimeException("요청한 상품 중 재고가 부족한 상품이 있습니다.");
        }

        Trade trade = Trade.builder()
                .member(member)
                .tradeStatus(TradeStatus.BUY)
                .tradeItems(new ArrayList<>())
                .build();

        member.getTrades().add(trade);

        // 변경된 코드
        Item item = itemRepository.findByIdForUpdate(requestItemDto.getItemId())
                .orElseThrow(() -> new RuntimeException("주문 하고자 하는 상품을 찾을 수 없습니다."));

        item.setStock(item.getStock() - requestItemDto.getQuantity());
        item.autoCheckQuantityForSetStatus();

        TradeItem tradeItem = TradeItem.builder()
                .trade(trade)
                .item(item)
                .quantity(requestItemDto.getQuantity())
                .build();

        tradeItem.setPrice();
        trade.addTradeItem(tradeItem);

        String tradeUUID = generateTradeUUID();
        trade.setTradeUUID(tradeUUID);
        trade.setTotalPrice(requestItemDto.getQuantity() * item.getPrice());

        try {
            portoneService.prePurchase(tradeUUID,new BigDecimal(trade.getTotalPrice()));
        } catch (Exception e) {
            throw new RuntimeException(e + " : 구매 대행사 오류로 인해 결제 요청에 실패하였습니다.");
        }

        //Trade 저장 (cascade 설정이 되어있다면 TradeItem도 함께 저장됨)
        tradeRepository.save(trade);

        //응답 객체 생성 및 반환
        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    // 포트원 PG 사 대신하여 결제 되었다고 처리할 수 있는 메서드
    public void processPayment(String uuid, int payAmount) {

        //Trade 조회
        Trade trade = tradeRepository.findByTradeUUID(uuid)
                .orElseThrow(() -> new RuntimeException("해당 거래를 찾을 수 없습니다: " + uuid));

        //이미 결제 완료된 거래인지 확인
        try {
            if (trade.getTradeStatus() == PAY) {
                throw new RuntimeException("이미 결제가 완료된 거래 입니다.");
            } else if (trade.getTradeStatus() == REFUSED) {
                throw new RuntimeException("주문이 이미 취소된 거래 입니다.");
            }

            //결제 금액과 지불 금액 일치 여부 확인 로직
            if (!trade.getTotalPrice().equals(payAmount)) {
                throw new RuntimeException("주문 금액과 결제 금액이 다릅니다.");
            }
        } catch (Exception e) {
            trade.setTradeStatus(REFUSED);
            rollBackProductQuantity(trade);
            throw e;
        }

        trade.setTradeStatus(PAY);
    }

    public void rollBackProductQuantity(Trade trade) {
        List<TradeItem> tradeItems = trade.getTradeItems();
        tradeItems.forEach(tradeItem -> {
            tradeItem.getItem().setStock(tradeItem.getItem().getStock() + tradeItem.getQuantity());
            tradeItem.getItem().autoCheckQuantityForSetStatus();
        });
    }


    public static String generateTradeUUID() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        String currentTime = dateFormat.format(new Date());
        String UUID = java.util.UUID.randomUUID().toString();
        byte[] UUIDStringBytes = UUID.getBytes(StandardCharsets.UTF_8);
        byte[] hashBytes;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            hashBytes = messageDigest.digest(UUIDStringBytes);
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
        return tradeItems.stream().mapToInt(item -> item.getItem().getPrice() * item.getQuantity()).sum();
    }



    private boolean stockValidCheck(OrderRequestItemDto requestDto) {
        //동시성 이슈 해결 - Pessimistic Lock
        Item item = itemRepository.findByIdForUpdate(requestDto.getItemId())
                .orElseThrow(() -> new RuntimeException("주문 하고자 하는 상품을 찾을 수 없습니다."));

        int reqQuantity = requestDto.getQuantity();
        int itemStock = item.getStock();

        if (reqQuantity < 0) {
            throw new RuntimeException("구매 수량은 0보다 작을 수 없습니다");
        }
        if (item.getItemStatus().equals(ItemStatus.SOLD_OUT)) {
            return false;
        }
        return reqQuantity <= itemStock;
    }
}
