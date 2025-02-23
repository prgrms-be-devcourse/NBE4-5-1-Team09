package com.example.cafe.domain.trade.controller.user;

import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.service.user.UserTradeAtomicUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Tag(name = "User Trade API", description = "사용자가 카드에 있는 상품을 주문하거나, 상품을 단건으로 주문하는 기능을 제공합니다.")
public class UserTradeController {
    private final UserTradeAtomicUpdateService service;

    @Operation(summary = "카트에 있는 상품 주문", description = "회원이 카트에 있는 상품을 주문할 수 있습니다.")
    @PostMapping("/cart")
    public ResponseEntity<OrderResponseDto> orderWithCart(@RequestParam("memberId") Long memberId) {
        return ResponseEntity.ok(service.tradeWithCart(memberId));
    }

    @Operation(summary = "카트에 넣지 않고 바로 상품 주문", description = "회원이 상품을 바로 주문할 수 있습니다.")
    @PostMapping("/item")
    public ResponseEntity<OrderResponseDto> orderWithItem(@RequestBody OrderRequestItemDto requestItemDto) {
        return ResponseEntity.ok(service.tradeWithItemInfo(requestItemDto));
    }
}
