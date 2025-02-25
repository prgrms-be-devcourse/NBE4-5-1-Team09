package com.example.cafe.domain.trade.service.user;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.repository.ItemRepository;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.CancelRequestDto;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.request.RePayRequestDto;
import com.example.cafe.domain.trade.domain.dto.response.CancelResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrdersResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.RePayResponseDto;
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
import java.util.stream.Collectors;

import static com.example.cafe.domain.trade.domain.dto.request.CancelRequestDto.*;
import static com.example.cafe.domain.trade.domain.dto.response.CancelResponseDto.*;
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
            throw new RuntimeException("주문 과정에서 오류 발생.");
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
            throw new RuntimeException("주문 과정에서 오류 발생.");
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
            OrderItemsDto orderItemsDto = new OrderItemsDto();
            orderItemsDto.setTradeUUID(trade.getTradeUUID());
            for (TradeItem tradeItem : tradeItems) {
                OrderItemDto itemDto = new OrderItemDto(tradeItem.getItem().getId(), tradeItem.getQuantity(), tradeItem.getItem().getItemName());
                orderItemsDto.getOrderItemDtoList().add(itemDto);
            }
            if (trade.getTradeStatus().equals(BUY)) {
                response.getBuyList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(PAY)) {
                response.getPayList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(PREPARE_DELIVERY)) {
                response.getPrepareDeliveryList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(BEFORE_DELIVERY)) {
                response.getBeforeDeliveryList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(IN_DELIVERY)) {
                response.getInDeliveryList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(POST_DELIVERY)) {
                response.getPostDeliveryList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(REFUSED)) {
                response.getRefusedList().add(orderItemsDto);
            }
            if (trade.getTradeStatus().equals(REFUND)) {
                response.getRefundList().add(orderItemsDto);
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

    public CancelResponseDto cancelTrade(Long memberId,CancelRequestDto requestDto) {
        Member member = getMember(memberId);
        Trade trade = tradeRepository.findByTradeUUID(requestDto.getTradeUUID())
                .orElseThrow(() -> new RuntimeException("해당 거래를 찾을 수 없습니다."));
        if (trade.getTradeStatus().equals(BUY)) {
            return cancelTradeOnBuy(member, requestDto, trade);
        } else if (trade.getTradeStatus().equals(PAY)) {
            return cancelTradeOnPay(member, requestDto, trade);
        } else {
            throw new RuntimeException("결제 이후 취소는 관리자에게 문의해주세요.");
        }
    }

    public CancelResponseDto cancelTradeOnBuy(Member member, CancelRequestDto requestDto, Trade trade) {

        List<CancelRequestDto.CancelItemRequest> cancelItemList = requestDto.getCancelItemList();
        List<TradeItem> cancelledItemsForNewTrade = new ArrayList<>();
        // 각 CancelItemRequest에 대해 처리: TradeItem 수량 감소 및 Item 재고 증가
        for (CancelRequestDto.CancelItemRequest cancelItem : cancelItemList) {
            Long cancelItemId = cancelItem.getItemId();
            int cancelQuantity = cancelItem.getQuantity();

            TradeItem tradeItem = trade.getTradeItems().stream()
                    .filter(ti -> ti.getItem().getId().equals(cancelItemId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("주문에서 해당 상품을 찾을 수 없습니다."));

            if (cancelQuantity > tradeItem.getQuantity()) {
                throw new RuntimeException("주문 수량보다 취소 수량이 많을 수 없습니다");
            }

            // TradeItem 수량 감소 및 Item 재고 증가
            tradeItem.setQuantity(tradeItem.getQuantity() - cancelQuantity);
            Item item = tradeItem.getItem();
            item.setStock(item.getStock() + cancelQuantity);

            // 취소된 수량에 대해 새로운 TradeItem 객체 생성(기존 값 복사)
            TradeItem cancelledTradeItem = new TradeItem();
            cancelledTradeItem.setItem(item);
            cancelledTradeItem.setQuantity(cancelQuantity);


            cancelledItemsForNewTrade.add(cancelledTradeItem);
        }

        // 기존 Trade의 tradeItems 컬렉션에서 수량이 0인 항목은 in-place로 제거 (컬렉션 전체 재할당 X)
        trade.getTradeItems().removeIf(ti -> ti.getQuantity() == 0);

        // 남은 상품들 확인
        List<TradeItem> remainingTradeItems = trade.getTradeItems();

        // 전체 취소: 남은 상품이 없으면 기존 Trade의 상태를 REFUSED로 변경
        if (remainingTradeItems.isEmpty()) {
            trade.setTradeStatus(TradeStatus.REFUSED);
            tradeRepository.save(trade);
            return new CancelResponseDto(trade.getId(), trade.getTradeUUID(), false, 0, new ArrayList<>());
        }

        // 부분 취소: 취소된 상품들로 새로운 Trade(상태: REFUSED) 생성
        Trade cancelledTrade = makeTrade(member, TradeStatus.REFUSED);
        cancelledTrade.setEmail(member.getEmail());
        cancelledTrade.setTradeItems(cancelledItemsForNewTrade);

        for (TradeItem tradeItem : cancelledItemsForNewTrade) {
            tradeItem.setTrade(cancelledTrade);
        }

        cancelledTrade.setTotalPrice(calculateTotalPrice(cancelledItemsForNewTrade));
        cancelledTrade.setAddress(trade.getAddress());
        // 임시로 기존 trade의 UUID 사용 (나중에 원래 Trade는 새 UUID 할당)
        cancelledTrade.setTradeUUID(trade.getTradeUUID());
        tradeRepository.save(cancelledTrade);

        // 원래 Trade에는 새 UUID 부여 후 재결제 요청
        trade.setTradeUUID(generateTradeUUID());
        trade.setTotalPrice(calculateTotalPrice(trade.getTradeItems()));
        BigDecimal remainingTotalPrice = BigDecimal.valueOf(calculateTotalPrice(remainingTradeItems));
        try {
            portoneService.prePurchase(trade.getTradeUUID(), remainingTotalPrice);
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("주문 과정에서 오류 발생.", e);
        }
        tradeRepository.save(trade);

        // 응답 DTO 구성 시, 취소 Trade의 TradeItem들을 DTO에 매핑하여 반환
        List<CancelResponseDto.RemainingTradeItemDto> cancelledDtoList = cancelledItemsForNewTrade.stream()
                .map(ti -> new CancelResponseDto.RemainingTradeItemDto(ti.getItem().getId(), ti.getQuantity()))
                .collect(Collectors.toList());

        return new CancelResponseDto(
                cancelledTrade.getId(),
                trade.getTradeUUID(),
                false,
                0,
                cancelledDtoList);

    }

    public CancelResponseDto cancelTradeOnPay(Member member, CancelRequestDto requestDto, Trade trade) {
        List<CancelRequestDto.CancelItemRequest> cancelItemList = requestDto.getCancelItemList();
        List<TradeItem> cancelledTradeItems = new ArrayList<>();

        // 기존 결제 금액(체크섬용) 보존
        BigDecimal checkSumAmount = BigDecimal.valueOf(trade.getTotalPrice());
        BigDecimal refundAmount = BigDecimal.ZERO;

        List<TradeItem> tradeItems = trade.getTradeItems();

        // 각 취소 요청 처리: TradeItem 수량 차감, 가격 재계산, 상품 재고 복구, 취소 TradeItem 생성
        for (CancelRequestDto.CancelItemRequest cancelItem : cancelItemList) {
            Long cancelItemId = cancelItem.getItemId();
            int cancelQuantity = cancelItem.getQuantity();

            TradeItem tradeItem = tradeItems.stream()
                    .filter(ti -> ti.getItem().getId().equals(cancelItemId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("주문에서 해당 상품을 찾을 수 없습니다."));

            if (cancelQuantity > tradeItem.getQuantity()) {
                throw new RuntimeException("취소 수량이 주문 수량보다 많을 수 없습니다.");
            }

            // 환불 금액 계산 (단가 * 취소 수량)
            int unitPrice = tradeItem.getItem().getPrice();
            BigDecimal itemRefund = BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(cancelQuantity));
            refundAmount = refundAmount.add(itemRefund);

            // 기존 TradeItem에서 취소 수량 차감 및 가격 업데이트
            tradeItem.setQuantity(tradeItem.getQuantity() - cancelQuantity);
            tradeItem.setPrice(); // 수량 변경 후 단가에 따른 가격 업데이트

            // 해당 상품 재고 복구
            Item item = tradeItem.getItem();
            item.setStock(item.getStock() + cancelQuantity);

            // 취소된 수량으로 새로운 TradeItem 객체 생성 (새 Trade에 추가할 용도)
            TradeItem cancelledTradeItem = TradeItem.builder()
                    .item(item)
                    .quantity(cancelQuantity)
                    .build();
            cancelledTradeItem.setPrice();
            cancelledTradeItems.add(cancelledTradeItem);
        }

        // 기존 Trade에서 남은 상품 목록 (수량 > 0) 추출
        List<TradeItem> remainingTradeItems = trade.getTradeItems().stream()
                .filter(ti -> ti.getQuantity() > 0)
                .collect(Collectors.toList());

        // 기존 Trade의 총 금액 재계산 (남은 상품 기준)
        int remainingTotalPrice = remainingTradeItems.stream()
                .mapToInt(TradeItem::getPrice)
                .sum();
        trade.setTotalPrice(remainingTotalPrice);

        // portoneService에 환불 요청 (기존 trade의 tradeUUID, 환불 금액, 원래 결제 금액 전달)
        portoneService.refund(trade.getTradeUUID(), refundAmount, checkSumAmount);

        // 전체 취소인 경우: 남은 상품이 없으면 기존 Trade의 상태를 REFUND로 업데이트
        if (remainingTradeItems.isEmpty()) {
            trade.setTradeStatus(TradeStatus.REFUND);
            tradeRepository.save(trade);

            List<RemainingTradeItemDto> responseDtoList = cancelledTradeItems.stream()
                    .map(ti -> new RemainingTradeItemDto(ti.getItem().getId(), ti.getQuantity()))
                    .collect(Collectors.toList());

            return new CancelResponseDto(
                    trade.getId(),
                    trade.getTradeUUID(),
                    true,
                    refundAmount.intValue(),
                    responseDtoList
            );
        } else {
            // 부분 취소인 경우:
            // • 기존 Trade는 상태 PAY 그대로 유지하며, 남은 상품들로 업데이트
            trade.setTradeItems(remainingTradeItems);
            tradeRepository.save(trade);

            // • 취소된 상품들은 별도의 새 Trade(상태 REFUND)로 생성하여 저장
            Trade cancelledTrade = makeTrade(member, TradeStatus.REFUND);
            cancelledTrade.setTradeUUID("refund" + trade.getTradeUUID());
            cancelledTrade.setTradeItems(cancelledTradeItems);
            int cancelledTotalPrice = cancelledTradeItems.stream()
                    .mapToInt(TradeItem::getPrice)
                    .sum();
            cancelledTrade.setTotalPrice(cancelledTotalPrice);
            member.getTrades().add(cancelledTrade);
            for (TradeItem cancelledTradeItem : cancelledTradeItems) {
                cancelledTradeItem.setTrade(cancelledTrade);
            }
            cancelledTrade.setTotalPrice(calculateTotalPrice(cancelledTrade.getTradeItems()));
            Trade savedCancelledTrade = tradeRepository.save(cancelledTrade);

            List<RemainingTradeItemDto> responseDtoList = cancelledTradeItems.stream()
                    .map(ti -> new RemainingTradeItemDto(ti.getItem().getId(), ti.getQuantity()))
                    .collect(Collectors.toList());

            return new CancelResponseDto(
                    savedCancelledTrade.getId(),
                    savedCancelledTrade.getTradeUUID(),
                    true,
                    refundAmount.intValue(),
                    responseDtoList
            );
        }
    }

    public void rollBackProductQuantity(Trade trade) {
        trade.getTradeItems().forEach(tradeItem -> {
            Item item = tradeItem.getItem();
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

    public RePayResponseDto payRetry(Long idFromToken, RePayRequestDto requestDto) {
        System.out.println("payRetry");
        System.out.println(requestDto.getTradeUUID());
        Integer totalPrice = tradeRepository.findByTradeUUID(requestDto.getTradeUUID()).orElseThrow(() -> new RuntimeException("해당 거래를 찾을 수 없습니다.")).getTotalPrice();
        return new RePayResponseDto(totalPrice);
    }
}