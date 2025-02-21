package com.example.cafe.domain.trade.service.user;


import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemStatus;
import com.example.cafe.domain.item.respository.ItemRepository;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemsDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
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

    /**
     * 1. 장바구니에서 주문 요청
     * 2. 상품 페이지에서 바로 주문 요청
     * 3. 결제 요청
     */

    public OrderResponseDto tradeWithCart(OrderRequestItemsDto requestItemsDto) {
        checkRequestQuantity(requestItemsDto);

        Trade trade = Trade.builder()
                .tradeStatus(TradeStatus.BUY)
                .tradeItems(new ArrayList<>())
                .build();

        for (OrderRequestItemDto orderRequestItemDto : requestItemsDto.getItemDtoList()) {
            Item item = itemRepository.findById(orderRequestItemDto.getItemId())
                    .orElseThrow(() -> new RuntimeException("해당 상품을 찾을 수 없습니다."));

            item.setStock(item.getStock() - orderRequestItemDto.getQuantity());
            item.autoCheckQuantityForSetStatus();

            TradeItem tradeItem = TradeItem.builder()
                    .trade(trade)
                    .item(item)
                    .quantity(orderRequestItemDto.getQuantity())
                    .build();

            tradeItem.setPrice();
            trade.addTradeItem(tradeItem);
        }

        trade.setTotalPrice(calculateTotalPrice(trade.getTradeItems()));

        String tradeUUID = generateTradeUUID();
        trade.setTradeUUID(tradeUUID);
        try {
            portoneService.prePurchase(tradeUUID, new BigDecimal(trade.getTotalPrice()));
        } catch (Exception e) {
            throw new RuntimeException(e + ": PG사 오류로 인해 결제 요청에 실패하였습니다.");
        }

        tradeRepository.save(trade);

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    public void tradeWithItemInfo() {

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


    private void checkRequestQuantity(OrderRequestItemsDto requestItemsDto) {
        boolean stockValidCheckResult = requestItemsDto.getItemDtoList().stream().allMatch(this::stockValidCheck);
        if (!stockValidCheckResult) {
            throw new RuntimeException("요청한 상품 중 재고가 부족한 상품이 있습니다.");
        }
    }

    private boolean stockValidCheck(OrderRequestItemDto requestDto) {
        Item item = itemRepository.findById(requestDto.getItemId()).orElseThrow(() -> new RuntimeException("해당 상품을 찾을 수 없습니다."));
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
