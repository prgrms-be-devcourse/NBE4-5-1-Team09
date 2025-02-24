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
import com.example.cafe.domain.trade.domain.entity.CartItem;
import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeItem;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import com.example.cafe.domain.trade.portone.service.PortoneService;
import com.example.cafe.domain.trade.repository.TradeRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.example.cafe.domain.trade.domain.entity.TradeStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTradeRedisLockService {

    private final TradeRepository tradeRepository;
    private final ItemRepository itemRepository;
    private final PortoneService portoneService;
    private final MemberRepository memberRepository;
    private final RedissonClient redissonClient;

    /**
     * Redis 분산 락을 획득하여 supplier.get()을 실행한 후 락을 해제하는 헬퍼 메서드.
     */
    private <T> T executeWithItemLock(Long itemId, Supplier<T> supplier) {
        String lockKey = "lock:item:" + itemId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("재고 확인을 위한 락 획득 실패 for item " + itemId);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생 for item " + itemId, e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 단일 상품 주문
     * Redis 락과 DB의 PESSIMISTIC_LOCK(findByIdForUpdate())을 함께 사용하여,
     * 재고 확인부터 차감, 주문 생성까지 보호합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderResponseDto tradeWithItemInfo(Long memberId,OrderRequestItemDto requestItemDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다"));

        return executeWithItemLock(requestItemDto.getItemId(), () -> {
            // DB 락을 통해 최신 재고를 조회
            Item item = itemRepository.findByIdForUpdate(requestItemDto.getItemId())
                    .orElseThrow(() -> new RuntimeException("주문 하고자 하는 상품을 찾을 수 없습니다."));

            int reqQuantity = requestItemDto.getQuantity();
            if (reqQuantity < 0) {
                throw new RuntimeException("구매 수량은 0보다 작을 수 없습니다");
            }
            if (item.getItemStatus().equals(ItemStatus.SOLD_OUT) || reqQuantity > item.getStock()) {
                throw new RuntimeException("재고가 부족합니다.");
            }

            // 재고 차감
            item.setStock(item.getStock() - reqQuantity);
            item.autoCheckQuantityForSetStatus();

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
        });
    }

    /**
     * 장바구니 주문
     * 여러 상품 주문 시, 데드락을 피하기 위해 먼저 중복 없는 상품 ID를 정렬한 후 각 상품에 대해 락을 획득하고,
     * DB 락(findByIdForUpdate())을 사용하여 각 상품의 최신 재고 상태를 확인 및 차감합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderResponseDto tradeWithCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다"));
        List<CartItem> cartItems;
        try {
            cartItems = member.getCart().getCartItems();
        } catch (NullPointerException e) {
            throw new RuntimeException("장바구니 카트가 비어있습니다.");
        }
        if (cartItems.isEmpty()) {
            throw new RuntimeException("장바구니 카트가 비어있습니다.");
        }

        // 중복 제거 및 정렬된 상품 ID 목록 생성
        Set<Long> itemIdSet = cartItems.stream()
                .map(ci -> ci.getItem().getId())
                .collect(Collectors.toSet());
        List<Long> sortedItemIds = new ArrayList<>(itemIdSet);
        Collections.sort(sortedItemIds);

        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            // 모든 관련 상품에 대해 Redis 락 획득
            for (Long itemId : sortedItemIds) {
                RLock lock = redissonClient.getLock("lock:item:" + itemId);
                if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("재고 확인을 위한 락 획득 실패 for item " + itemId);
                }
                acquiredLocks.add(lock);
            }

            // 보호 영역 내에서 각 상품별 최신 재고를 DB 락으로 확인
            for (CartItem cartItem : cartItems) {
                Item item = itemRepository.findByIdForUpdate(cartItem.getItem().getId())
                        .orElseThrow(() -> new RuntimeException("주문하고자 하는 상품을 찾을 수 없습니다."));
                int reqQuantity = cartItem.getQuantity();
                if (reqQuantity < 0) {
                    throw new RuntimeException("구매 수량은 0보다 작을 수 없습니다");
                }
                if (item.getItemStatus().equals(ItemStatus.SOLD_OUT) || reqQuantity > item.getStock()) {
                    throw new RuntimeException("요청한 상품 중 재고가 부족한 상품이 있습니다.");
                }
            }

            // 모든 상품에 대해 재고 차감 처리
            for (CartItem cartItem : cartItems) {
                Item item = itemRepository.findByIdForUpdate(cartItem.getItem().getId())
                        .orElseThrow(() -> new RuntimeException("주문하고자 하는 상품을 찾을 수 없습니다."));
                int reqQuantity = cartItem.getQuantity();
                item.setStock(item.getStock() - reqQuantity);
                item.autoCheckQuantityForSetStatus();
            }

            // Trade 생성
            Trade trade = makeTrade(member, BUY);
            member.getTrades().add(trade);

            for (CartItem cartItem : cartItems) {
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            // 획득한 Redis 락 모두 해제
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
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
