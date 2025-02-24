package com.example.cafe.domain.trade.controller.user;

import com.example.cafe.domain.member.service.AuthTokenService;
import com.example.cafe.domain.trade.domain.dto.request.CancelRequestDto;
import com.example.cafe.domain.trade.domain.dto.request.OrderRequestItemDto;
import com.example.cafe.domain.trade.domain.dto.response.CancelResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrderResponseDto;
import com.example.cafe.domain.trade.domain.dto.response.OrdersResponseDto;
import com.example.cafe.domain.trade.service.user.UserTradeAtomicUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Tag(name = "User Trade API", description = "사용자가 카드에 있는 상품을 주문하거나, 상품을 단건으로 주문하는 기능을 제공합니다.")
public class UserTradeController {
    private final AuthTokenService authTokenService;
    private final UserTradeAtomicUpdateService service;

    @Operation(summary = "카트에 있는 상품 주문", description = "회원이 카트에 있는 상품을 주문할 수 있습니다.")
    @PostMapping("/cart")
    public ResponseEntity<OrderResponseDto> orderWithCart(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(service.tradeWithCart(authTokenService.getIdFromToken(authHeader)));
    }

    @Operation(summary = "카트에 넣지 않고 바로 상품 주문", description = "회원이 상품을 바로 주문할 수 있습니다.")
    @PostMapping("/item")
    public ResponseEntity<OrderResponseDto> orderWithItem(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody OrderRequestItemDto requestItemDto) {
        return ResponseEntity.ok(service.tradeWithItemInfo(authTokenService.getIdFromToken(authHeader), requestItemDto));
    }

    @Operation(summary = "사용자가 주문한 주문 목록 전체 조회", description = "회원 자신의 주문 목록 전체를 조회할 수 있습니다.")
    @GetMapping("/show")
    public ResponseEntity<OrdersResponseDto> showTradeList(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        return ResponseEntity.ok(service.showAllTradeItems(authTokenService.getIdFromToken(authHeader)));
    }

    @Operation(summary = "사용자가 주문 후 특정 상품 취소", description = "취소 희망 상품 ID, 수량을 List 로 받을 수 있습니다.")
    @PostMapping("cancel/buy")
    public ResponseEntity<CancelResponseDto> cancelOnBuy(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CancelRequestDto cancelRequestDto
    ) {
        return ResponseEntity.ok(service.cancelTrade(authTokenService.getIdFromToken(authHeader), cancelRequestDto));
    }

}
