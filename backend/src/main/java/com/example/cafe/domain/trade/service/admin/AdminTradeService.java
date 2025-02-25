package com.example.cafe.domain.trade.service.admin;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.trade.domain.dto.request.AdminConfirmRequestDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrdersResponseDto;
import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeItem;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import com.example.cafe.domain.trade.portone.service.PortoneService;
import com.example.cafe.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.cafe.domain.trade.domain.entity.TradeStatus.*;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminTradeService {

    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;
    private final PortoneService portoneService;


    //주문이 PAY 이면 -> TradeStatus 변경 (2시 이전 , 2시 이후)
    public OrderResponseDto adminConfirm(AdminConfirmRequestDto confirmRequestDto) {
        Trade trade = getTrade(confirmRequestDto);

        if (!trade.getTradeStatus().equals(PAY)) {
            throw new RuntimeException("결제 완료 상태에서만 배송 상태로 변경할 수 있습니다.");
        }
        LocalDateTime updatedDate = trade.getTradeUpdatedDate();
        LocalDateTime twoPmToday = updatedDate.toLocalDate().atTime(14, 0);

        if (updatedDate.isBefore(twoPmToday)) {
            //2시 이전
            trade.setTradeStatus(TradeStatus.BEFORE_DELIVERY);
        } else {
            //2시 이후
            trade.setTradeStatus(PREPARE_DELIVERY);
        }

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    // PREPARE_DELIVERY -> BEFORE_DELIVERY 상태 변경

    public OrderResponseDto adminPrepareDelivery(AdminConfirmRequestDto prepareRequestDto) {
        Trade trade = getTrade(prepareRequestDto);

        if (!trade.getTradeStatus().equals(PREPARE_DELIVERY)) {
            throw new RuntimeException("배송 대기 상태에서만 배송 준비로 상태 변경이 가능합니다.");
        }

        trade.setTradeStatus(TradeStatus.BEFORE_DELIVERY);

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    public OrderResponseDto adminSetInDelivery(AdminConfirmRequestDto inDeliveryRequestDto) {
        Trade trade = getTrade(inDeliveryRequestDto);

        if (!trade.getTradeStatus().equals(TradeStatus.BEFORE_DELIVERY)) {
            throw new RuntimeException("배송 대기 상태에서만 배송 중 으로 변경 가능합니다.");
        }

        trade.setTradeStatus(TradeStatus.IN_DELIVERY);

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    public OrderResponseDto adminSetPostDelivery(AdminConfirmRequestDto postDeliveryRequestDto) {
        Trade trade = getTrade(postDeliveryRequestDto);

        if (!trade.getTradeStatus().equals(TradeStatus.IN_DELIVERY)) {
            throw new RuntimeException("배송 중 상태에서만 배송 완료로 변경 가능합니다.");
        }

        trade.setTradeStatus(TradeStatus.POST_DELIVERY);

        return new OrderResponseDto(
                trade.getId(),
                trade.getTradeStatus(),
                trade.getTotalPrice(),
                trade.getTradeUUID()
        );
    }

    //관리자 주문 전체 조회
    public OrdersResponseDto showAllTradeItems() {
        List<Long> allMemberIds = memberRepository.findAllMemberIds();
        List<Trade> trades = new ArrayList<>();

        for (Long allMemberId : allMemberIds) {
            Member member = getMember(allMemberId);
            List<Trade> memberTrades = member.getTrades();
            trades.addAll(memberTrades);
        }
        OrdersResponseDto response = new OrdersResponseDto();
        for (Trade trade : trades) {
            List<TradeItem> tradeItems = trade.getTradeItems();
            OrdersResponseDto.OrderItemsDto orderItemsDto = new OrdersResponseDto.OrderItemsDto();
            orderItemsDto.setTradeUUID(trade.getTradeUUID());
            for (TradeItem tradeItem : tradeItems) {
                OrdersResponseDto.OrderItemDto itemDto = new OrdersResponseDto.OrderItemDto(tradeItem.getItem().getId(), tradeItem.getQuantity(), tradeItem.getItem().getItemName());
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

    private Trade getTrade(AdminConfirmRequestDto requestDto) {
        Trade trade = tradeRepository.findByTradeUUID(requestDto.getTradeUUID())
                .orElseThrow(() -> new RuntimeException("TradeUUID : [" + requestDto.getTradeUUID() + "] 해당 거래를 찾을 수 없습니다."));
        return trade;
    }
}

